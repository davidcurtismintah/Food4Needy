package com.allow.food4needy.profile.donationssummary

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allow.food4needy.R
import com.allow.food4needy.common.EventObserver
import kotlinx.android.synthetic.main.fragment_donations_summary.*


class DonationsSummaryFragment : Fragment() {

    companion object {
        fun newInstance(): DonationsSummaryFragment = DonationsSummaryFragment()
    }

    interface DonationsSummaryFragmentInteractionListener {

    }

    private var mInteractionListener: DonationsSummaryFragmentInteractionListener? = null

    private lateinit var mViewModel: DonationsSummaryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_donations_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefresh.run {
            setOnRefreshListener {
                mViewModel.createdDonationsLiveData.getDonationsSummary()
            }
        }
        mViewModel = ViewModelProviders.of(this).get(DonationsSummaryViewModel::class.java)
        mViewModel.createdDonationsLiveData.observe(this, Observer {
            it?.let {
                showUserCreatedDonations(it)
            }
        })
        mViewModel.donatedDonationsLiveData.observe(this, Observer {
            it?.let {
                showUserDonatedDonations(it)
            }
        })
        mViewModel.expiredDonationsLiveData.observe(this, Observer {
            it?.let {
                showUserExpiredDonations(it)
            }
        })
        mViewModel.loadingLiveData.observe(this, Observer {
            it?.peekContent()?.let {
                swipeRefresh.isRefreshing = it
            }
        })
        mViewModel.showSnackBarLiveData.observe(this, EventObserver {
            showSnackBar()
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mInteractionListener = if (context is DonationsSummaryFragmentInteractionListener) context
        else throw RuntimeException("${context.toString()} must implement DonationsSummaryFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        mInteractionListener = null
    }

    private fun showUserCreatedDonations(created: Long) {
        donations_created.text = if (created == 0L) getString(R.string.empty_info_pending_text) else created.toString()
    }

    private fun showUserDonatedDonations(donated: Long) {
        donations_donated.text = if (donated == 0L) getString(R.string.empty_info_donated_text) else donated.toString()
    }

    private fun showUserExpiredDonations(expired: Long) {
        donations_expired.text = if (expired == 0L) getString(R.string.empty_info_expired_text) else expired.toString()
    }

    private fun showSnackBar() {
        Snackbar.make(scrollView,
                getString(R.string.error_could_not_get_user_donation_summary),
                Snackbar.LENGTH_SHORT).show()
    }

}
