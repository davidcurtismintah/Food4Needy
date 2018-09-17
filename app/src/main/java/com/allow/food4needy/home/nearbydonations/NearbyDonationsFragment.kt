package com.allow.food4needy.home.nearbydonations

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.allow.food4needy.R
import com.allow.food4needy.common.EmptyContentView.Companion.NO_LABEL
import com.allow.food4needy.common.EventObserver
import com.allow.food4needy.domain.*
import com.allow.food4needy.home.nearbydonations.PickDonationBottomSheet.PickDonationBottomSheetInteractionListener
import kotlinx.android.synthetic.main.fragment_list.*
import com.allow.food4needy.home.BaseNearbyDataFragment
import com.allow.food4needy.home.BaseNearbyDataRecyclerAdapter
import com.allow.food4needy.home.HomeViewModel
import com.firebase.geofire.GeoLocation

class NearbyDonationsFragment : BaseNearbyDataFragment<Donation, NearbyDonationsItemViewHolder>(),
        PickDonationBottomSheetInteractionListener {

    companion object {
        private const val DONATION_USER_ROLE = "donation_user_role"

        fun newInstance(role: UserRole): NearbyDonationsFragment {
            val bundle = Bundle()
            bundle.putSerializable(DONATION_USER_ROLE, role)
            val fragment = NearbyDonationsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    interface NearbyDonationsFragmentInteractionListener : BaseDataFragmentInteractionListener

    private lateinit var mViewModel: NearbyDonationsViewModel

    private val mPickUpDonationBottomSheetDialog by lazy { PickDonationBottomSheet() }

    override var mAdapter: BaseNearbyDataRecyclerAdapter<Donation, NearbyDonationsItemViewHolder> = BaseNearbyDataRecyclerAdapter.makeDonationsRecyclerAdapter(
            actionOnPickDonation = {
                if (mViewModel.currentDonation.value == null) {
                    mViewModel.pickDonationLiveData.pickUpDonation(it)
                } else {
                    Snackbar.make(my_donations, getString(R.string.loading_text), Snackbar.LENGTH_LONG).show()
                }
            },
            actionOnExpire = {
                mViewModel.expireDonationLiveData.expireDonation(it)
            }
    )

    override val emptyDataImageResId: Int
        get() = when (mDonationUserRole) {
            UserRole.DONOR -> R.drawable.ic_empty_donations
            UserRole.RESTAURANT -> R.drawable.ic_empty_restaurants
            else -> throw IllegalArgumentException("DonationUserRole can only be one of ${UserRole.DONOR} or ${UserRole.RESTAURANT}")
        }

    override val emptyDataDescriptionResId: Int
        get() = when (mDonationUserRole) {
            UserRole.DONOR -> R.string.empty_nearby_donations_text
            UserRole.RESTAURANT -> R.string.empty_nearby_restaurants_text
            else -> throw IllegalArgumentException("DonationUserRole can only be one of ${UserRole.DONOR} or ${UserRole.RESTAURANT}")
        }

    override val emptyDataActionLabelResId: Int
        get() = when (mDonationUserRole) {
            UserRole.DONOR -> R.string.empty_nearby_donations_action_text
            UserRole.RESTAURANT -> R.string.empty_nearby_restaurants_action_text
            else -> throw IllegalArgumentException("DonationUserRole can only be one of ${UserRole.DONOR} or ${UserRole.RESTAURANT}")
        }

    override val emptyDataClickAction: () -> Unit = {
        mInteractionListener?.showAddDonationActivity()
    }

    private val mDonationUserRole: UserRole
        get() = arguments?.getSerializable(DONATION_USER_ROLE) as? UserRole
                ?: throw IllegalArgumentException("DonationUserRole cannot be null")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProviders.of(this, NearbyDonationsViewModelFactory(mDonationUserRole)).get(NearbyDonationsViewModel::class.java)
        mViewModel.pickDonationLiveData.observe(this, EventObserver { response ->
            when (response.status) {
                Status.LOADING -> {
                    showLoading(false)
                }

                Status.SUCCESS -> {
                    mViewModel.currentDonation.value = response.data
                    response.data?.let { mPickUpDonationBottomSheetDialog.show(childFragmentManager, "PickDonationBottomSheet") }
                }

                Status.ERROR -> {
                    mViewModel.currentDonation.value = null
                    Snackbar.make(my_donations, getString(R.string.pick_donation_error_text), Snackbar.LENGTH_LONG).show()
                    showLoading(false)
                }
            }
        })

        mViewModel.getNearbyDonationsLiveData.observe(this, Observer {
            handleNearbyDataResponse(it)
        })

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mInteractionListener = if (context is NearbyDonationsFragmentInteractionListener) context
        else throw RuntimeException("${context.toString()} must implement ${NearbyDonationsFragmentInteractionListener::class.java.simpleName}")
    }

    override fun onDetach() {
        super.onDetach()
        mInteractionListener = null
    }

    override fun resetCurrentDonation() {
        showLoading(false)
        mViewModel.currentDonation.value = null
    }

    override fun onReceiveLocationUpdate(geoLocation: GeoLocation) {
        mViewModel.getNearbyDonationsLiveData.fetchDonations(geoLocation)
    }
}
