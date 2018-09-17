package com.allow.food4needy.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allow.food4needy.R
import com.allow.food4needy.common.BottomPaddingItemDecoration
import com.allow.food4needy.common.EmptyContentView
import com.allow.food4needy.domain.UserRole
import kotlinx.android.synthetic.main.fragment_list.*

abstract class BaseDataFragment : Fragment() {

    interface BaseDataFragmentInteractionListener {
        fun showAddDonationActivity()
        fun showToolbarAndBottomNav(disableScroll: Boolean)
    }

    protected var mInteractionListener: BaseDataFragmentInteractionListener? = null
    abstract val emptyDataImageResId: Int
    abstract val emptyDataDescriptionResId: Int
    abstract val emptyDataActionLabelResId: Int
    abstract val emptyDataClickAction: (() -> Unit)

    protected abstract fun setupList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loading_progress.hide()

        with(swipeRefresh) {
            isEnabled = false
        }
        with(recyclerview) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity).apply {
                reverseLayout = true
                stackFromEnd = true
            }
        }

        setupList()

        val homeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
        homeViewModel.listBottomPadding.observe(this, Observer {
            it?.let {
                recyclerview.addItemDecoration(BottomPaddingItemDecoration(it))
            }
        })
        homeViewModel.userRoleLiveData.observe(this, Observer { role ->
            with(empty_list_view) {
                emptyDataImageResId.let(::setImage)
                emptyDataDescriptionResId.let(::setDescription)
                emptyDataActionLabelResId.let {
                    when (role) {
                        UserRole.GUEST -> setActionLabel(EmptyContentView.NO_LABEL)
                        UserRole.DONOR -> setActionLabel(it)
                        UserRole.RESTAURANT -> setActionLabel(it)
                        UserRole.VOLUNTEER -> setActionLabel(EmptyContentView.NO_LABEL)
                    }
                }
                emptyDataClickAction.let {
                    when (role) {
                        UserRole.GUEST -> setActionClickedListener(null)
                        UserRole.DONOR -> setActionClickedListener(it)
                        UserRole.RESTAURANT -> setActionClickedListener(it)
                        UserRole.VOLUNTEER -> setActionClickedListener(null)
                    }
                }

            }
        })


    }

    protected fun showEmptyView(show: Boolean) {
        empty_list_view.visibility = if (show) View.VISIBLE else View.GONE
        swipeRefresh.visibility = if (show) View.GONE else View.VISIBLE

        mInteractionListener?.showToolbarAndBottomNav(show)

    }

    protected fun showLoading(show: Boolean) {
        if (show) {
            loading_progress.show()
        } else {
            loading_progress.hide()
        }
    }
}