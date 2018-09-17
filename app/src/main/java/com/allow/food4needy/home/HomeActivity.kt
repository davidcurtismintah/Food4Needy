package com.allow.food4needy.home

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.allow.food4needy.BuildConfig
import com.allow.food4needy.Food4NeedyApplication
import com.allow.food4needy.R
import com.allow.food4needy.adddonation.AddDonationActivity
import com.allow.food4needy.common.*
import com.allow.food4needy.domain.UserRole
import com.allow.food4needy.home.mydonations.MyDonationsFragment
import com.allow.food4needy.home.nearbydonations.NearbyDonationsFragment
import com.allow.food4needy.home.nearbyvolunteers.NearbyVolunteersFragment
import com.allow.food4needy.profile.ProfileActivity
import com.allow.food4needy.start.StartActivity
import com.crashlytics.android.Crashlytics
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.view_toolbar_customview.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber


class HomeActivity : AppCompatActivity(),
        MyDonationsFragment.MyDonationsFragmentInteractionListener,
        NearbyDonationsFragment.NearbyDonationsFragmentInteractionListener,
        NearbyVolunteersFragment.NearbyVolunteersFragmentInteractionListener,
        EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    companion object {
        const val RC_FINE_LOCATION = 100
        const val RC_ADD_ACTIVITY = 200

        private const val EXTRA_ROLE = "user_role"
        private const val CONFIG_ALLOWED = "allowed"
        private const val CONFIG_NOT_ALLOWED_MSG = "not_allowed_msg"

        fun startIntent(context: Context, userRole: UserRole): Intent {
            val intent = Intent(context, HomeActivity::class.java)
            intent.putExtra(EXTRA_ROLE, userRole)
            return intent
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        container.setCurrentItem(item.order, false)
        return@OnNavigationItemSelectedListener true
    }

    private lateinit var mViewModel: HomeViewModel

    private val mFirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Food4NeedyApplication.userManager.userLoggedIn) showStartActivity()

        initRemoteConfig()

        setContentView(R.layout.activity_home)

        container.setPagingEnabled(false)
        container.offscreenPageLimit = 3

        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setDisplayShowHomeEnabled(false)
            setDisplayShowTitleEnabled(false)

            val inflater = LayoutInflater.from(this@HomeActivity)

            val mCustomView = inflater.inflate(R.layout.view_toolbar_customview, coordinator, false)
            customView = mCustomView
            setDisplayShowCustomEnabled(true)
        }

        navigation_bar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        mViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        mViewModel.userRoleLiveData.observe(this, Observer {
            Timber.d( "role: $it")
            it?.let {role->
                val roleAdapter = when (role) {
                    UserRole.GUEST -> GuestRoleAdapter()
                    UserRole.DONOR -> DonorRoleAdapter()
                    UserRole.VOLUNTEER -> VolunteerRoleAdapter()
                    UserRole.RESTAURANT -> RestaurantRoleAdapter()
                }
                val sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, roleAdapter)
                container.adapter = sectionsPagerAdapter

                user_profile_image.visibility = if (role == UserRole.GUEST) View.GONE else View.VISIBLE

                mViewModel.userNameLiveData.value?.run {
                    welcome_user_message.text =
                            if (role == UserRole.GUEST) getString(R.string.toolbar_welcome_guest_text) else
                                getString(R.string.toolbar_welcome_text, this)
                }
                toolbar_layout.setOnClickListener {
                    if (role == UserRole.GUEST) return@setOnClickListener
                    startActivity(ProfileActivity.getIntent(this))
                }

                user_profile_facebook_icon.setOnClickListener {
                    val facebookIntent = Intent(Intent.ACTION_VIEW)
                    facebookIntent.data = Uri.parse("https://www.facebook.com/food4needyinternational/")
                    startActivity(facebookIntent)
                }

                with(fab_add_donations) {
                    if ((role != UserRole.DONOR) && (role != UserRole.RESTAURANT)) {
                        hide()
                    } else {
                        show()
                        fab_add_donations.setOnClickListener {
                            showAddDonationActivityInternal()
                        }
                        post {
                            mViewModel.listBottomPadding.value = height
                        }
                    }
                }

//                startTrackerService()

            }
        })
        mViewModel.userImageLiveData.observe(this, Observer {
            it?.let {
                user_profile_image.loadImage(it, true, R.drawable.ic_face_black_24dp)
            }
        })
        mViewModel.userNameLiveData.observe(this, Observer {
            it?.let {
                mViewModel.userRoleLiveData.value?.run {
                    welcome_user_message.text =
                            if ((this == UserRole.GUEST) || it.isEmpty()) getString(R.string.toolbar_welcome_guest_text) else
                                getString(R.string.toolbar_welcome_text, it)
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (checkLocationServicesAvailability()) {
            startTrackerService()
        }
    }

    private fun showStartActivity() {
        val intent = Intent(this@HomeActivity, StartActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showAddDonationActivityInternal() {
        val startIntent = Intent(this@HomeActivity, AddDonationActivity::class.java)
        startIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivityForResult(startIntent, RC_ADD_ACTIVITY)
    }

    override fun showAddDonationActivity() {
        showAddDonationActivityInternal()
    }

    override fun showToolbarAndBottomNav(disableScroll: Boolean) {

        val params = navigation_bar.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as BottomNavigationBehavior
        val consumed = IntArray(2)
        behavior.onNestedPreScroll(coordinator, navigation_bar, search_edit_frame, 0, -Integer.MAX_VALUE, consumed, ViewCompat.TYPE_TOUCH)

        search_edit_frame.setExpanded(true)

    }

    @AfterPermissionGranted(RC_FINE_LOCATION)
    private fun startTrackerService() {
        if (hasLocationPermission) {
            reallyStartLocationTracking()
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location),
                    RC_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun reallyStartLocationTracking() {
        val intent = MyIntentBuilder.getInstance(this, commandId = MyIntentBuilder.START).build()
        if (isPreAndroidO()) {
            Timber.d("moveToStartedState: Running on Android N or lower - startService(intent)")
            startService(intent)
        } else {
            Timber.d("moveToStartedState: Running on Android O - startForegroundService(intent)")
            startForegroundService(intent)
        }
    }

    private val hasLocationPermission
        get() =
            EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Timber.d("onPermissionsGranted:$requestCode:$perms.size")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Timber.d("onPermissionsDenied:$requestCode:$perms.size")
        if (!EasyPermissions.hasPermissions(this, *perms.toTypedArray())) {
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
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val yes = getString(R.string.yes)
                    val no = getString(R.string.no)
                    Toast.makeText(
                            this,
                            getString(R.string.returned_from_app_settings_to_activity,
                                    if (hasLocationPermission) yes else no),
                            Toast.LENGTH_LONG)
                            .show()
                }
            }
            HomeActivity.RC_ADD_ACTIVITY -> if (resultCode == Activity.RESULT_OK) {
//                showToolbarAndBottomNav(false)
                showSnackBar(true)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User Cancelled the action
            }
        }
    }

    inner class SectionsPagerAdapter(fm: FragmentManager, private val roleAdapter: RoleAdapter) : FragmentStatePagerAdapter(fm) {

        init {
            navigation_bar.inflateMenu(roleAdapter.navMenuResId)
        }

        override fun getItem(position: Int): Fragment = roleAdapter.getItem(position)

        override fun getCount() = roleAdapter.getCount()
    }

    private fun showSnackBar(success: Boolean) {
        Snackbar.make(coordinator,
                if (!success) getString(R.string.error_could_not_add_donation) else getString(R.string.success_donation_added),
                Snackbar.LENGTH_SHORT).show()
    }


    private fun initRemoteConfig() {

        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        mFirebaseRemoteConfig.setConfigSettings(configSettings)
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)

        val allowed = mFirebaseRemoteConfig.getBoolean(CONFIG_ALLOWED)
        if (!allowed){
            val notAllowedMessage = mFirebaseRemoteConfig.getString(CONFIG_NOT_ALLOWED_MSG)
            Toast.makeText(this, notAllowedMessage, Toast.LENGTH_LONG).show()
            finish()
        }

        val task: Task<Void>
        if (mFirebaseRemoteConfig.info.configSettings.isDeveloperModeEnabled) {
            task = mFirebaseRemoteConfig.fetch(0)
        } else {
            task = mFirebaseRemoteConfig.fetch()
        }
        task.addOnCompleteListener(this) { result ->
            if (result.isSuccessful) {
                mFirebaseRemoteConfig.activateFetched()
            }
        }

    }

}