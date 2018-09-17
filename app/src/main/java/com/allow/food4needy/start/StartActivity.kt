package com.allow.food4needy.start

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import com.allow.food4needy.BuildConfig
import com.allow.food4needy.Food4NeedyApplication
import com.allow.food4needy.R
import com.allow.food4needy.common.addFragment
import com.allow.food4needy.common.addFragmentToStack
import com.allow.food4needy.common.removeFragment
import com.allow.food4needy.common.replaceFragment
import com.allow.food4needy.domain.UserRole
import com.allow.food4needy.home.HomeActivity
import com.allow.food4needy.start.adduser.AddUserFragment
import com.allow.food4needy.start.adduser.AddUserFragment.UserDetailsFragmentInteractionListener
import com.allow.food4needy.start.selectrole.SelectRoleFragment
import com.allow.food4needy.start.selectrole.SelectRoleFragment.SelectRoleFragmentInteractionListener
import com.allow.food4needy.start.skipsignin.SkipSignInFragment
import com.allow.food4needy.start.skipsignin.SkipSignInFragment.SkipSignInFragmentInteractionListener
import com.allow.food4needy.start.welcome.WelcomeFragment
import com.allow.food4needy.start.welcome.WelcomeFragment.WelcomeFragmentInteractionListener
import com.crashlytics.android.Crashlytics
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.android.support.DaggerAppCompatActivity
import timber.log.Timber


class StartActivity : DaggerAppCompatActivity(),
        WelcomeFragmentInteractionListener,
        SelectRoleFragmentInteractionListener,
        UserDetailsFragmentInteractionListener,
        SkipSignInFragmentInteractionListener {

    companion object {
        const val RC_SIGN_IN = 123
    }

//    @Inject
//    lateinit var mWelcomeFragment: Lazy<WelcomeFragment>
//
//    @Inject
//    lateinit var mSelectRoleFragment: Lazy<SelectRoleFragment>
//
//    @Inject
//    lateinit var mSignUpFragment: Lazy<SignUpFragment>
//
//    @Inject
//    lateinit var mLoginFragment: Lazy<LoginFragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Food4NeedyApplication.userManager.userLoggedIn)
            showHomeActivity(Food4NeedyApplication.userManager.currentUser.role)

        setContentView(R.layout.activity_start)

        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.start_container)
        if (fragment == null) {
            addFragment(R.id.start_container, WelcomeFragment.newInstance())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data)
        }
    }

    private fun handleSignInResponse(resultCode: Int, data: Intent?) {
        val response = IdpResponse.fromResultIntent(data)

        // Successfully signed in
        if (resultCode == Activity.RESULT_OK) {
            addFragment(R.id.start_container, SelectRoleFragment.newInstance())
        } else {
            addFragment(R.id.start_container, WelcomeFragment.newInstance())
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled)
                return
            }

            if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection)
                return
            }

            showSnackbar(R.string.unknown_error)
            Timber.e(response.error, "handleSignInResponse:Sign-in error")
        }
    }

    override fun showFireBaseAuthUi() {
        val startIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setAvailableProviders(
                        listOf(
                                AuthUI.IdpConfig.PhoneBuilder().build()
                        )
                ).build()
        startIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivityForResult(
                startIntent,
                RC_SIGN_IN)
        supportFragmentManager.findFragmentById(R.id.start_container)?.let { removeFragment(it) }
    }

    override fun showUserDetailsFragment(userRole: UserRole) {
        replaceFragment(R.id.start_container, AddUserFragment.newInstance(userRole))
    }

    override fun showLogInFragment() {
        showFireBaseAuthUi()
    }

    override fun showSkipSignInFragment() {
        addFragmentToStack(R.id.start_container, SkipSignInFragment())
    }

    override fun showHomeActivity(userRole: UserRole) {
        startActivity(HomeActivity.startIntent(this, userRole))
        finish()
    }

    private fun showSnackbar(@StringRes id: Int) {
        Snackbar.make(findViewById(R.id.start_container), resources.getString(id), Snackbar.LENGTH_LONG).show()
    }

}

