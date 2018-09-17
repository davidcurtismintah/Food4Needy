package com.allow.food4needy.home

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allow.food4needy.R
import com.allow.food4needy.domain.Donation
import com.allow.food4needy.domain.NearbyData
import com.allow.food4needy.domain.User
import com.allow.food4needy.home.nearbydonations.NearbyDonationsItemViewHolder
import com.allow.food4needy.home.nearbyvolunteers.NearbyVolunteersItemViewHolder
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

abstract class BaseNearbyDataRecyclerAdapter<D : NearbyData, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    companion object {

        private val MAX_DIFF_TIME_MS: Long = 1000

        private val mBackgroundDiffThread = HandlerThread("diffing-thread")
        private val mCancelingThread = HandlerThread("canceling-thread")

        init {
            if (!mBackgroundDiffThread.isAlive) {
                mBackgroundDiffThread.start()
            }
            if (!mCancelingThread.isAlive) {
                mCancelingThread.start()
            }
        }

        private val mBackgroundHandler = Handler(mBackgroundDiffThread.looper)
        private val mCancelingHandler = Handler(mCancelingThread.looper)
        private val mMainHandler = Handler(Looper.getMainLooper())

        //------------------ Factory
        class DonationsRecyclerAdapter(private val actionOnPickDonation: (Donation) -> Unit,
                                       private val actionOnExpire: (Donation) -> Unit
        ) : BaseNearbyDataRecyclerAdapter<Donation, NearbyDonationsItemViewHolder>() {
            override val layoutId: Int = R.layout.list_item_nearby_donations
            override fun Donation.copyData(): Donation = copy()
            override fun Donation.isSameItem(other: Donation): Boolean = id == other.id
            override fun Donation.hasSameContent(other: Donation): Boolean = this == other
            override fun createViewHolder(itemView: View): NearbyDonationsItemViewHolder = NearbyDonationsItemViewHolder(itemView, actionOnPickDonation, actionOnExpire)
            override fun NearbyDonationsItemViewHolder.bind(data: Donation) = bindViews(data)
            override fun NearbyDonationsItemViewHolder.unbind() = unbindViews()
        }

        class VolunteersRecyclerAdapter(private val callButtonAction: (User) -> Unit
        ) : BaseNearbyDataRecyclerAdapter<User, NearbyVolunteersItemViewHolder>() {
            override val layoutId: Int = R.layout.list_item_volunteers
            override fun User.copyData(): User = copy()
            override fun User.isSameItem(other: User): Boolean = id == other.id
            override fun User.hasSameContent(other: User): Boolean = this == other
            override fun createViewHolder(itemView: View): NearbyVolunteersItemViewHolder = NearbyVolunteersItemViewHolder(itemView, callButtonAction)
            override fun NearbyVolunteersItemViewHolder.bind(data: User) = bindViews(data)
            override fun NearbyVolunteersItemViewHolder.unbind() = unbindViews()
        }

        fun makeDonationsRecyclerAdapter(actionOnPickDonation: (Donation) -> Unit, actionOnExpire: (Donation) -> Unit) = DonationsRecyclerAdapter(actionOnPickDonation, actionOnExpire)

        fun makeVolunteersRecyclerAdapter(callButtonAction: (User) -> Unit) = VolunteersRecyclerAdapter(callButtonAction)
    }

    private val mDataVersion = AtomicInteger(0)
    private var mData: List<D>? = null

    private var mRecyclerView: RecyclerView? = null

    private inner class DiffRequest(internal var oldItems: List<D>, internal var newItems: List<D>) {

        internal val dataVersion: Int = mDataVersion.incrementAndGet()

        internal var result: DiffUtil.DiffResult? = null

    }

    private class DiffTimeoutException : RuntimeException()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = createViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = mData?.get(position)
        d?.let { holder.bind(it) }
    }

    override fun getItemCount() = mData?.size ?: 0

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    fun refresh(dataItems: List<D>) {
        if (mRecyclerView == null) {
            return
        }
        val newItems = ArrayList<D>(dataItems.size)
        // Deep-copy the new data in case someone else mutates those objects
        for (item in dataItems) {
            newItems.add(item.copyData())
        }

        if (mData == null) {
            mData = newItems
            notifyItemRangeInserted(0, newItems.size)
        } else {
            // Make sure the oldItems list doesn't mutate during diff
            val oldItems = ArrayList(mData)

            val request = DiffRequest(oldItems, newItems)
            computeDiff(request)
        }
    }


    private fun computeDiff(request: DiffRequest) {
        mBackgroundHandler.post {
            try {
                reallyComputeDiff(request)
            } catch (ignored: DiffTimeoutException) {
            }

            mMainHandler.post { applyRequest(request) }
        }
    }

    private fun applyRequest(request: DiffRequest) {
        if (mDataVersion.toInt() != request.dataVersion) {
            return
        }
        mData = request.newItems
        if (request.result == null) {
            notifyDataSetChanged()
        } else {
            request.result!!.dispatchUpdatesTo(this)
        }
    }

    private fun reallyComputeDiff(request: DiffRequest) {
        val isCanceled = AtomicBoolean(false)
        val isDone = AtomicBoolean(false)
        val canceledLock = Any()
        val cancelRunnable = Runnable {
            synchronized(canceledLock) {
                if (isDone.get()) {
                    return@synchronized
                }
                isCanceled.set(true)
            }
        }
        mCancelingHandler.postDelayed(cancelRunnable, MAX_DIFF_TIME_MS)

        request.result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            private fun checkCanceled() {
                if (isCanceled.get()) {
                    throw DiffTimeoutException()
                }
            }

            override fun getOldListSize(): Int {
                checkCanceled()
                return request.oldItems.size
            }

            override fun getNewListSize(): Int {
                checkCanceled()
                return request.newItems.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                checkCanceled()
                return request.oldItems[oldItemPosition].isSameItem(request.newItems[newItemPosition])
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                checkCanceled()
                return request.oldItems[oldItemPosition].hasSameContent(request.newItems[newItemPosition])
            }

        })
        mCancelingHandler.removeCallbacks(cancelRunnable)
        synchronized(canceledLock) {
            isDone.set(true)
            isCanceled.set(false)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mRecyclerView = null
    }

    abstract fun D.copyData(): D
    abstract fun D.isSameItem(other: D): Boolean
    abstract fun D.hasSameContent(other: D): Boolean
    abstract fun createViewHolder(itemView: View): VH
    abstract val layoutId: Int
    abstract fun VH.bind(data: D)
    abstract fun VH.unbind()
}




