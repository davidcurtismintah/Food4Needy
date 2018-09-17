package com.allow.food4needy.home.mydonations

import android.Manifest
import android.app.Activity.RESULT_OK
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.allow.food4needy.Food4NeedyApplication
import com.allow.food4needy.R
import com.allow.food4needy.common.BottomPaddingItemDecoration
import com.allow.food4needy.common.EventObserver
import com.allow.food4needy.common.mGson
import com.allow.food4needy.domain.Donation
import com.allow.food4needy.domain.DonationState
import com.allow.food4needy.domain.Response
import com.allow.food4needy.domain.Status
import com.allow.food4needy.home.BaseDataFragment
import com.allow.food4needy.home.HomeViewModel
import com.allow.food4needy.home.mydonations.DeleteDonationDialog.DeleteDonationDialogFragmentInteractionListener
import com.allow.food4needy.home.mydonations.DonateDonationBottomSheet.DonateDonationBottomSheetInteractionListener
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_list.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class MyDonationsFragment : BaseDataFragment(),
        DonateDonationBottomSheetInteractionListener,
        DeleteDonationDialogFragmentInteractionListener {

    companion object {
        private const val PERM_RC_CAPTURE_IMAGE = 100
        private const val REQUEST_CAPTURE_IMAGE = 200

        fun newInstance(): MyDonationsFragment {
            return MyDonationsFragment()
        }
    }

    interface MyDonationsFragmentInteractionListener : BaseDataFragmentInteractionListener

    private lateinit var mAdapter: MyDonationsRecyclerAdapter

    private lateinit var mViewModel: MyDonationsViewModel
    private val mBottomSheetDialog by lazy { DonateDonationBottomSheet() }
//    private val mDeleteDialog by lazy { DeleteDonationDialog() }

    override val emptyDataImageResId: Int = R.drawable.ic_empty_donations
    override val emptyDataDescriptionResId: Int = R.string.empty_my_donations_text
    override val emptyDataActionLabelResId: Int = R.string.empty_my_donations_action_text
    override val emptyDataClickAction: () -> Unit = { mInteractionListener?.showAddDonationActivity() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
        homeViewModel.listBottomPadding.observe(this, Observer {
            it?.let {
                recyclerview.addItemDecoration(BottomPaddingItemDecoration(it))
            }
        })

        mViewModel = ViewModelProviders.of(this).get(MyDonationsViewModel::class.java)
        mViewModel.donateDonationLiveData.observe(this, EventObserver {
            processDonateResponse(it)
        })
        mViewModel.deleteDonationLiveData.observe(this, EventObserver {
            processDeleteResponse(it)
        })
        mViewModel.postSelfieLiveData.observe(this, EventObserver {
            processPostSelfieResponse(it)
        })
    }

    override fun setupList() {
        val query = FirebaseDatabase.getInstance().getReference("user-donations").child(Food4NeedyApplication.userManager.userId).child("state").child("${DonationState.CREATED.ordinal}").child("all").child("data")
        val options: FirebaseRecyclerOptions<Donation> = FirebaseRecyclerOptions.Builder<Donation>()
                .setQuery(query) {
                    val jsonString = mGson.toJson(it.value)
                    mGson.fromJson(jsonString, Donation::class.java)
                }
                .setLifecycleOwner(this)
                .build()
        mAdapter = MyDonationsRecyclerAdapter(options,
                onDataChangedAction = { itemCount ->
                    showEmptyView(itemCount == 0)
                },
                actionOnDonate = {
                    if (mViewModel.currentDonation.value == null) {
                        mViewModel.donateDonationLiveData.donateDonation(it)
                    } else {
                        Snackbar.make(my_donations, getString(R.string.loading_text), Snackbar.LENGTH_LONG).show()
                    }

                },
                actionOnDelete = {
                    if (mViewModel.currentDonation.value == null) {
                        mViewModel.currentDonation.value = it
                        val mDeleteDialog = DeleteDonationDialog.newInstance(it.frequency)
                        mDeleteDialog.show(childFragmentManager, "Delete Donation Dialog")
                    } else {
                        Snackbar.make(my_donations, getString(R.string.loading_text), Snackbar.LENGTH_LONG).show()
                    }
                },
                actionOnExpire = {
                    mViewModel.expireDonationLiveData.expireDonation(it)
                }
        )

        // Scroll to bottom on new item
        mAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                recyclerview.smoothScrollToPosition(mAdapter.itemCount)
            }
        })

        recyclerview.adapter = mAdapter
    }

    private fun processDonateResponse(response: Response<Donation, String>) {
        when (response.status) {
            Status.LOADING -> renderDonateLoadingState()

            Status.SUCCESS -> response.data?.let(::renderDonateDataState)

            Status.ERROR -> response.error?.let(::renderDonateErrorState)
        }
    }

    private fun renderDonateLoadingState() {
        showLoading(true)
    }

    private fun renderDonateDataState(data: Donation) {
        mViewModel.currentDonation.value = data
        mBottomSheetDialog.show(childFragmentManager, "Donate Bottom Sheet")
        showLoading(false)
    }

    private fun renderDonateErrorState(error: String) {
        Timber.e(error)
        Snackbar.make(my_donations, getString(R.string.my_donation_donate_error_text), Snackbar.LENGTH_LONG).show()
        resetCurrentDonation()
    }

    private fun processDeleteResponse(response: Response<String, String>) {
        when (response.status) {
            Status.LOADING -> renderDeleteLoadingState()

            Status.SUCCESS -> response.data?.let(::renderDeleteDataState)

            Status.ERROR -> response.error?.let(::renderDeleteErrorState)
        }
    }

    private fun renderDeleteLoadingState() {
        showLoading(true)
    }

    private fun renderDeleteDataState(donationName: String) {
        Snackbar.make(my_donations, getString(R.string.my_donation_deleted_text, donationName), Snackbar.LENGTH_LONG).show()
        resetCurrentDonation()
    }

    private fun renderDeleteErrorState(error: String) {
        Timber.e(error)
        Snackbar.make(my_donations, getString(R.string.my_donation_delete_error_text), Snackbar.LENGTH_LONG).show()
        resetCurrentDonation()
    }

    private fun processPostSelfieResponse(response: Response<String, String>) {
        when (response.status) {
            Status.LOADING -> renderPostSelfieLoadingState()

            Status.SUCCESS -> response.data?.let { renderPostSelfieDataState() }

            Status.ERROR -> response.error?.let(::renderPostSelfieErrorState)
        }
    }

    private fun renderPostSelfieLoadingState() {
        showLoading(true)
    }

    private fun renderPostSelfieDataState() {
        Snackbar.make(my_donations, getString(R.string.my_donation_selfie_posted_text), Snackbar.LENGTH_LONG).show()
        showLoading(false)
    }

    private fun renderPostSelfieErrorState(error: String) {
        Timber.e(error)
        Snackbar.make(my_donations, getString(R.string.my_donation_selfie_post_error_text), Snackbar.LENGTH_LONG).show()
        showLoading(false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mInteractionListener = if (context is MyDonationsFragmentInteractionListener) context
        else throw RuntimeException("${context.toString()} must implement MyDonationsFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        mInteractionListener = null
    }

    override fun showCameraView() {
        if (hasCameraPermission) {
            reallyShowCameraView()
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_camera),
                    PERM_RC_CAPTURE_IMAGE,
                    Manifest.permission.CAMERA)
        }
    }

    @AfterPermissionGranted(PERM_RC_CAPTURE_IMAGE)
    private fun reallyShowCameraView() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(context!!.packageManager) != null) {
            startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE)
        } else {
            Toast.makeText(swipeRefresh.context, getString(R.string.error_camera_app_not_installed), Toast.LENGTH_LONG).show()
        }
    }

    private val hasCameraPermission
        get() =
            EasyPermissions.hasPermissions(context!!, Manifest.permission.CAMERA)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if (data != null && data.extras != null) {
                val imageBitmap = data.extras!!.get("data") as Bitmap
                Timber.i("Image captured: $imageBitmap")

                mViewModel.postSelfieLiveData.makePost(imageBitmap)
            }
        }
    }

    override fun deleteDonation() {
        mViewModel.currentDonation.value?.let {
            mViewModel.deleteDonationLiveData.deleteDonation(it)
        }
    }

    override fun cancelDeleteDonation() {

    }

    override fun resetCurrentDonation() {
        showLoading(false)
        mViewModel.currentDonation.value = null
    }

}
