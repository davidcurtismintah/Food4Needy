package com.allow.food4needy.home.nearbydonations

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.allow.food4needy.R
import com.allow.food4needy.common.EventObserver
import com.allow.food4needy.domain.Response
import com.allow.food4needy.domain.Status
import com.allow.food4needy.domain.User
import kotlinx.android.synthetic.main.view_pick_up_donation.*
import kotlinx.android.synthetic.main.view_pick_up_donation.view.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber


class PickDonationBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val RC_CALL_PHONE = 100
    }

    interface PickDonationBottomSheetInteractionListener {
        fun resetCurrentDonation()
    }

    private lateinit var mViewModel: PickDonationBottomSheetViewModel
    private lateinit var mParentFragmentModel: NearbyDonationsViewModel

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.view_pick_up_donation, container, false)

        view.fab_make_call.setOnClickListener {
            makeCall()
        }
        view.fab_start_navigation.setOnClickListener {
            startNavigation()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mParentFragmentModel = ViewModelProviders.of(parentFragment!!).get(NearbyDonationsViewModel::class.java)
        mParentFragmentModel.currentDonation.observe(this, Observer {
            it?.let { donation ->
                mViewModel.userLiveData.getUser(donation.userId)
            }
        })

        mViewModel = ViewModelProviders.of(this).get(PickDonationBottomSheetViewModel::class.java)
        mViewModel.userLiveData.observe(this, EventObserver {
            processUserResponse(it)
        })
    }

    private fun processUserResponse(response: Response<User, String>) {
        when (response.status) {
            Status.LOADING -> renderUserLoadingState()

            Status.SUCCESS -> response.data?.let(::renderUserDataState)

            Status.ERROR -> response.error?.let(::renderUserErrorState)
        }
    }

    private fun renderUserLoadingState() {
        // no op
    }

    private fun renderUserDataState(data: User) {
        volunteer_name.text = data.name
        volunteer_phone.text = data.phone
        volunteer_address.text = data.address
    }

    private fun renderUserErrorState(error: String) {
        Timber.e(error)
    }

    @AfterPermissionGranted(RC_CALL_PHONE)
    private fun makeCall() {
        if (hasCallPermission) {
            reallyMakeCall()
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_call),
                    RC_CALL_PHONE,
                    Manifest.permission.CALL_PHONE)
        }
    }

    private fun reallyMakeCall() {
        mViewModel.userLiveData.value?.peekContent()?.data?.phone?.let { phone ->
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$phone")
            if (callIntent.resolveActivity(context!!.packageManager) != null) {
                startActivity(callIntent);
            } else {
                Toast.makeText(fab_make_call.context, getString(R.string.error_dialer_app_not_installed), Toast.LENGTH_LONG).show()
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

    private fun startNavigation() {
        mParentFragmentModel.currentDonation.value?.let { donation ->
            val gmmIntentUri = Uri.parse("google.navigation:q=${donation.latitude},${donation.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(context!!.packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(fab_make_call.context, getString(R.string.error_google_maps_not_installed), Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        (parentFragment as? PickDonationBottomSheetInteractionListener)?.resetCurrentDonation()
    }

}