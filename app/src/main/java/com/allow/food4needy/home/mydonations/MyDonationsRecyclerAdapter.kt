package com.allow.food4needy.home.mydonations

import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import com.allow.food4needy.R
import com.allow.food4needy.domain.Donation
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class MyDonationsRecyclerAdapter(option: FirebaseRecyclerOptions<Donation>,
                                 private val onDataChangedAction: (itemCount: Int) -> Unit,
                                 private val actionOnDonate: (Donation) -> Unit,
                                 private val actionOnDelete: (Donation) -> Unit,
                                 private val actionOnExpire: (Donation) -> Unit
)
    : FirebaseRecyclerAdapter<Donation, MyDonationsItemViewHolder>(option) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MyDonationsItemViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_my_donations, parent, false), actionOnDonate, actionOnDelete, actionOnExpire)

    override fun onBindViewHolder(holder: MyDonationsItemViewHolder, position: Int, model: Donation) {
        holder.bindViews(getRef(position).key ?: "", model)
    }

    override fun onDataChanged() {
        onDataChangedAction.invoke(snapshots.size)
    }

}
