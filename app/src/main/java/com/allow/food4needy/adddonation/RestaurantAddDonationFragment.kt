package com.allow.food4needy.adddonation

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.allow.food4needy.R
import com.allow.food4needy.common.EventObserver
import com.allow.food4needy.adddonation.ItemPickerDialog.ItemPickerDialogInteractionListener
import com.allow.food4needy.domain.Donation
import com.allow.food4needy.domain.DonationFrequency
import com.allow.food4needy.domain.Response
import com.allow.food4needy.domain.Status
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_restaurant_add_donation.*
import kotlinx.android.synthetic.main.view_progress_results.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class RestaurantAddDonationFragment : Fragment(),
        ItemPickerDialogInteractionListener {

    companion object {
        const val RC_FINE_LOCATION = 100
    }

    interface RestaurantAddDonationFragmentInteractionListener {
        fun finishActivity()
    }

    private var mInteractionListener: RestaurantAddDonationFragmentInteractionListener? = null

    private lateinit var mViewModel: AddDonationViewModel
    private var selectedItem = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_restaurant_add_donation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt(ItemPickerDialog.ARG_SELECTED_INDEX, -1)
        }

        with(fab_add) {
            post {
                val marginParams = add_donation_layout.layoutParams as ViewGroup.MarginLayoutParams
                marginParams.setMargins(
                        marginParams.leftMargin,
                        marginParams.topMargin,
                        marginParams.rightMargin,
                        height)
                add_donation_layout.requestLayout()
            }
            setOnClickListener {
                startSubmitDonation()
            }
        }

        oneTimeSwitch.setOnCheckedChangeListener { _, isChecked ->
            showOneTimeView(isChecked)
        }

        rl_pick_frequency.setOnClickListener {
            val pickerItems = listOf(
                    ItemPickerDialog.Item(DonationFrequency.DAILY.name.capitalize(), DonationFrequency.DAILY.ordinal),
                    ItemPickerDialog.Item(DonationFrequency.WEEKLY.name.capitalize(), DonationFrequency.WEEKLY.ordinal),
                    ItemPickerDialog.Item(DonationFrequency.MONTHLY.name.capitalize(), DonationFrequency.MONTHLY.ordinal),
                    ItemPickerDialog.Item(DonationFrequency.ANNUALLY.name.capitalize(), DonationFrequency.ANNUALLY.ordinal)
            )
            val dialog = ItemPickerDialog.newInstance(
                    activity!!.getString(R.string.select_offer_dialog_title),
                    pickerItems,
                    selectedItem
            )
            dialog.show(childFragmentManager, "ItemPicker")
        }

        mViewModel = ViewModelProviders.of(this).get(AddDonationViewModel::class.java)
//        mViewModel.loadingLiveData.observe(this, Observer {
//            it?.peekContent()?.let(::showLoading)
//        })
//        mViewModel.showSnackBarLiveData.observe(this, EventObserver {
//            showSnackBar(it)
//        })
//        mViewModel.finishLiveData.observe(this, EventObserver {
//            mInteractionListener?.finishActivity()
//        })
        mViewModel.addDonationLiveData.observe(this, EventObserver {
            processAddDonationResponse(it)
        })

    }

    private fun processAddDonationResponse(response: Response<Donation, String>) {
        when (response.status) {
            Status.LOADING -> renderAddDonationLoadingState()

            Status.SUCCESS -> response.data?.let { renderAddDonationDataState() }

            Status.ERROR -> response.error?.let { renderAddDonationErrorState() }
        }
    }

    private fun renderAddDonationLoadingState() {
        showLoading(true)
    }

    private fun renderAddDonationDataState() {
//        showSnackBar(true)
        mInteractionListener?.finishActivity()
    }

    private fun renderAddDonationErrorState() {
//        showSnackBar(false)
        mInteractionListener?.finishActivity()
    }

    private fun showOneTimeView(checked: Boolean) {
        recurring_donation_card_view.visibility = if (checked) View.GONE else View.VISIBLE
        one_time_card_view.visibility = if (checked) View.VISIBLE else View.GONE
    }

    private fun showSnackBar(success: Boolean) {
        Snackbar.make(scrollView,
                if (!success) getString(R.string.error_could_not_add_donation) else getString(R.string.success_donation_added),
                Snackbar.LENGTH_SHORT).show()
    }

    @AfterPermissionGranted(RC_FINE_LOCATION)
    private fun startSubmitDonation() {
        if (hasLocationPermission) {

            if (oneTimeSwitch.isChecked) {
                submitOneTimeDonation()
            } else {
                submitRecurringDonation()
            }

        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location),
                    RC_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun submitOneTimeDonation() {
        val foodName = food_name.text.toString()
        val foodWeight = food_weight.text.toString()
        val foodExpiry = food_expiry.text.toString()

        var verified = true
        if (TextUtils.isEmpty(foodName)) {
            food_name_layout.error = getString(R.string.error_donation_name_is_required)
            verified = false
        }
        if (TextUtils.isEmpty(foodWeight)) {
            food_weight_layout.error = getString(R.string.error_donation_weight_is_required)
            verified = false
        }
        if (TextUtils.isEmpty(foodExpiry)) {
            food_expiry_layout.error = getString(R.string.error_donation_expiry_is_required)
            verified = false
        }

        if (!verified) return

        val client = LocationServices.getFusedLocationProviderClient(context!!)
        val permission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.lastLocation
                    .addOnSuccessListener {
                        it?.let {
                            mViewModel.submitDonation(it, DonationFrequency.ONETIME, foodName, foodWeight, foodExpiry)
                        }
                    }
        }
    }

    private fun submitRecurringDonation() {
        val offerName = recurring_donation_name.text.toString()
        val selectedOffer = recurring_donation_frequency.text.toString()

        var verified = true
        if (TextUtils.isEmpty(offerName)) {
            recurring_donation_name_layout.error = getString(R.string.error_donation_name_is_required)
            verified = false
        }
        if (TextUtils.isEmpty(selectedOffer)) {
            view_empty_recurring_donation_frequency.error = getString(R.string.error_donation_frequency_is_required)
            verified = false
        }

        if (!verified) return

        val client = LocationServices.getFusedLocationProviderClient(context!!)
        val permission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.lastLocation
                    .addOnSuccessListener {
                        it?.let {
                            mViewModel.submitDonation(it, DonationFrequency.valueOf(selectedOffer), offerName)
                        }
                    }
        }
    }

    private val hasLocationPermission
        get() =
            EasyPermissions.hasPermissions(context!!, Manifest.permission.ACCESS_FINE_LOCATION)

    private fun showLoading(loading: Boolean) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(scrollView.windowToken, 0)

        add_donation_done_view.visibility = if (loading) View.VISIBLE else View.GONE
        done_imageview.run {
            scaleX = 0f
            scaleY = 0f
            animate()
                    .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                    .scaleX(1f)
                    .scaleY(1f)
        }
        scrollView.visibility = if (loading) View.GONE else View.VISIBLE
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mInteractionListener = if (context is RestaurantAddDonationFragmentInteractionListener) context
        else throw RuntimeException("${context.toString()} must implement RestaurantAddDonationFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        mInteractionListener = null
    }

    override fun onItemSelected(fragment: ItemPickerDialog, item: ItemPickerDialog.Item, index: Int) {
        selectedItem = index

        recurring_donation_frequency.text = item.title
        recurring_donation_frequency.visibility = View.VISIBLE
        view_empty_recurring_donation_frequency.text = ""
        view_empty_recurring_donation_frequency.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ItemPickerDialog.ARG_SELECTED_INDEX, selectedItem)
    }
}

