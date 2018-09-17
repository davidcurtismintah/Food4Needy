package com.allow.food4needy.home.nearbyvolunteers

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.allow.food4needy.R
import com.allow.food4needy.common.EmptyContentView
import com.allow.food4needy.domain.User
import com.allow.food4needy.home.BaseNearbyDataFragment
import com.allow.food4needy.home.BaseNearbyDataRecyclerAdapter
import com.allow.food4needy.home.nearbydonations.PickDonationBottomSheet
import com.firebase.geofire.GeoLocation
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class NearbyVolunteersFragment : BaseNearbyDataFragment<User, NearbyVolunteersItemViewHolder>() {

    companion object {
        fun newInstance(): NearbyVolunteersFragment {
            return NearbyVolunteersFragment()
        }
    }

    interface NearbyVolunteersFragmentInteractionListener : BaseDataFragmentInteractionListener

    private lateinit var mViewModel: NearbyVolunteersViewModel

    override var mAdapter: BaseNearbyDataRecyclerAdapter<User, NearbyVolunteersItemViewHolder> = BaseNearbyDataRecyclerAdapter.makeVolunteersRecyclerAdapter(
            callButtonAction = {
                mViewModel.currentVolunteer.value = it
                makeCall()
            }
    )
    override val emptyDataImageResId: Int = R.drawable.ic_empty_volunteers
    override val emptyDataDescriptionResId: Int = R.string.empty_nearby_volunteers_text
    override val emptyDataActionLabelResId: Int = EmptyContentView.NO_LABEL
    override val emptyDataClickAction: (() -> Unit) = { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProviders.of(this).get(NearbyVolunteersViewModel::class.java)
        mViewModel.getNearbyVolunteersLiveData.observe(this, Observer {
            handleNearbyDataResponse(it)
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mInteractionListener = if (context is NearbyVolunteersFragmentInteractionListener) context
        else throw RuntimeException("${context.toString()} must implement ${NearbyVolunteersFragmentInteractionListener::class.java.simpleName}")
    }

    override fun onDetach() {
        super.onDetach()
        mInteractionListener = null
    }

    @AfterPermissionGranted(PickDonationBottomSheet.RC_CALL_PHONE)
    private fun makeCall() {
        if (hasCallPermission) {
            reallyMakeCall()
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_call),
                    PickDonationBottomSheet.RC_CALL_PHONE,
                    Manifest.permission.CALL_PHONE)
        }
    }

    private fun reallyMakeCall() {
        mViewModel.currentVolunteer.value?.phone?.let { phone ->
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$phone")
            if (callIntent.resolveActivity(context!!.packageManager) != null) {
                startActivity(callIntent)
            } else {
                Toast.makeText(context, getString(R.string.error_dialer_app_not_installed), Toast.LENGTH_LONG).show()
            }
        }
    }

    private val hasCallPermission
        get() =
            EasyPermissions.hasPermissions(context!!, Manifest.permission.CALL_PHONE)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onReceiveLocationUpdate(geoLocation: GeoLocation) {
        mViewModel.getNearbyVolunteersLiveData.fetchDonations(geoLocation)
    }
}
