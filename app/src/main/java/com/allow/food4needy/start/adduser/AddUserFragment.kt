package com.allow.food4needy.start.adduser


import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.allow.food4needy.Food4NeedyApplication
import com.allow.food4needy.R
import com.allow.food4needy.domain.Response
import com.allow.food4needy.domain.Status
import com.allow.food4needy.domain.User
import com.allow.food4needy.domain.UserRole
import com.allow.food4needy.start.StartViewModel
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.fragment_add_user.*
import kotlinx.android.synthetic.main.view_progress_results.*
import kotlinx.android.synthetic.main.view_progress_results.view.*
import kotlinx.android.synthetic.main.view_signup_pick_location.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject

class AddUserFragment @Inject constructor() : Fragment(),
        AddUserView,
        EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    companion object {
        private const val ARG_ROLE = "arg_role"

        private const val RC_FINE_LOCATION = 100
        private const val RC_PLACE_PICKER_REQUEST = 200

        fun newInstance(userRole: UserRole): AddUserFragment {
            val fragment = AddUserFragment()
            val args = Bundle()
            args.putSerializable(ARG_ROLE, userRole)
            fragment.arguments = args
            return fragment
        }
    }

    interface UserDetailsFragmentInteractionListener {
        fun showHomeActivity(userRole: UserRole)
    }

    private var mInteractionListener: UserDetailsFragmentInteractionListener? = null

    private val mUserDetailsPresenter by lazy {
        AddUserPresenterImpl(
                viewAdd = this,
                userRole = (arguments?.getSerializable(ARG_ROLE) as? UserRole)!!,
                userManager = Food4NeedyApplication.userManager)
    }

    private var formValidated: Boolean = false
        get() {
            field = true
            if (TextUtils.isEmpty(user_name.text)) {
                user_name_layout.error = getString(R.string.error_user_details_name_is_required)
                field = false
            }
            if (TextUtils.isEmpty(user_phone.text)) {
                user_phone_layout.error = getString(R.string.error_user_details_phone_is_required)
                field = false
            }
            return field
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        user_name_layout.hint = getString(R.string.user_name_hint_text, mUserDetailsPresenter.userRole.name.toLowerCase().capitalize())

        rlPickLocation.setOnClickListener {
            placePickerTask()
        }

        proceed.setOnClickListener {

            if (!formValidated) return@setOnClickListener

            val user = User(
                    role = mUserDetailsPresenter.userRole,
                    imageUrl = "",
                    name = user_name.text.toString(),
                    email = user_email.text.toString(),
                    phone = user_phone.text.toString(),
                    alternatePhone = "",
                    address = placeAddress.text.toString()
            )

            mUserDetailsPresenter.updateUserDetails(user)
        }

        with(view) {
            if (savedInstanceState == null) {
                user_details_parent.post {
                    done_imageview.run {
                        scaleX = 0f
                        scaleY = 0f
                        animate()
                                .setDuration(resources.getInteger(android.R.integer.config_longAnimTime).toLong())
                                .scaleX(1f)
                                .scaleY(1f)
                    }
                }
            }
        }

        val startViewModel = ViewModelProviders.of(activity!!).get(StartViewModel::class.java)
        startViewModel.userLiveData.observe(this, Observer {
            it?.let {
                processShowUserResponse(it)
            }
        })
    }

    private fun processShowUserResponse(response: Response<User, String>) {
        when (response.status) {
            Status.LOADING -> renderShowUserLoadingState()

            Status.SUCCESS -> response.data?.let(::renderShowUserDataState)

            Status.ERROR -> response.error?.let{ renderShowUserErrorState() }
        }
    }

    private fun renderShowUserLoadingState() {
        showLoading(true)
        done_imageview.visibility = View.GONE
    }

    private fun renderShowUserDataState(user: User) {
        showLoading(false)
        done_imageview.visibility = View.VISIBLE
        if (user.role == mUserDetailsPresenter.userRole) {
            setUpViews(user)
            setupAddressViews("", user.address)
        } else {
            mUserDetailsPresenter.start()
        }
    }

    private fun renderShowUserErrorState() {
        showLoading(false)
        done_imageview.visibility = View.VISIBLE
//        Toast.makeText(context, getString(R.string.error_could_not_get_user), Toast.LENGTH_SHORT).show()
        mUserDetailsPresenter.start()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mInteractionListener = if (context is UserDetailsFragmentInteractionListener) context
        else throw RuntimeException("${context.toString()} must implement UserDetailsFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        mInteractionListener = null
    }

    override fun setUpViews(user: User) {
//        user_image.loadImage(user.imageUrl, true, R.drawable.ic_photo_camera)
        user_name.setText(user.name)
        user_email.setText(user.email)
        user_phone.setText(user.phone)
        placeAddress.text = user.address
    }

    override fun showHomeView(userRole: UserRole) {
        mInteractionListener?.showHomeActivity(userRole)
    }

    override fun showLoading(loading: Boolean) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(user_details_parent.windowToken, 0)

        auth_done_view.visibility = if (loading) View.VISIBLE else View.GONE
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

    @AfterPermissionGranted(RC_FINE_LOCATION)
    private fun placePickerTask() {
        if (hasLocationPermission) {
            startPlacePicker()
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location),
                    RC_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startPlacePicker() {
        val builder = PlacePicker.IntentBuilder()
        try {
            val intent = builder.build(activity)
            startActivityForResult(intent, RC_PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    private val hasLocationPermission
        get() =
            EasyPermissions.hasPermissions(context!!, Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Timber.d("onPermissionsGranted:$requestCode:$perms.size")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Timber.d("onPermissionsDenied:$requestCode:$perms.size")
        if (!EasyPermissions.hasPermissions(context!!, *perms.toTypedArray())) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                AppSettingsDialog.Builder(this).build().show()
            }
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        Timber.d("onRationaleAccepted:$requestCode")
    }

    override fun onRationaleDenied(requestCode: Int) {
        Timber.d("onRationaleDenied:$requestCode")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE -> {
                val yes = getString(R.string.yes)
                val no = getString(R.string.no)
                Toast.makeText(
                        activity,
                        getString(R.string.returned_from_app_settings_to_activity,
                                if (hasLocationPermission) yes else no),
                        Toast.LENGTH_LONG)
                        .show()
            }
            RC_PLACE_PICKER_REQUEST -> if (resultCode == Activity.RESULT_OK) {
                val place = PlacePicker.getPlace(activity, data)
                setupAddressViews(place.name, place.address)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(activity, getString(R.string.no_place_selected_text), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupAddressViews(where: CharSequence, address: CharSequence?) {

        if (TextUtils.isEmpty(address) && (TextUtils.isEmpty(address))) return

//        placeName.text = where
        placeAddress.text = if (!TextUtils.isEmpty(where)) getString(R.string.add_user_address_text, where, address) else address

        viewEmptyAddress.visibility = View.GONE
//        placeName.visibility = View.VISIBLE
        placeSeparator.visibility = View.VISIBLE
        placeAddress.visibility = View.VISIBLE
    }

}
