package com.allow.food4needy.start.welcome


import android.arch.lifecycle.Lifecycle
import android.content.Context
import android.os.Bundle
import android.support.animation.DynamicAnimation
import android.support.animation.SpringAnimation
import android.support.animation.SpringForce
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allow.food4needy.Food4NeedyApplication
import com.allow.food4needy.R
import com.allow.food4needy.domain.UserRole
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_welcome.*
import javax.inject.Inject

class WelcomeFragment @Inject constructor() : DaggerFragment(), WelcomeView {

    interface WelcomeFragmentInteractionListener {
        fun showFireBaseAuthUi()
        fun showLogInFragment()
        fun showHomeActivity(userRole: UserRole)
        fun showSkipSignInFragment()
    }

    companion object {
        fun newInstance() = WelcomeFragment()
    }

    private var mInteractionListener: WelcomeFragmentInteractionListener? = null

    private val mWelcomePresenter by lazy { WelcomePresenterImpl(this, Food4NeedyApplication.userManager) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view) {
            if (savedInstanceState == null) {
                welcome_layout.post {
                    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return@post
                    welcome_bg.run {
                        scaleX = 1.5f
                        val scaleXAnim = SpringAnimation(welcome_bg, DynamicAnimation.SCALE_X)
                        val springX = SpringForce().apply {
                            finalPosition = 1f
                            stiffness = SpringForce.STIFFNESS_LOW
                            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                        }
                        scaleXAnim.spring = springX
                        scaleXAnim.start()
                    }

                    logo.run {
                        scaleX = 0f
                        scaleY = 0f
                        animate()
                                .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                                .scaleX(1f)
                                .scaleY(1f)
                    }

                    welcome_message.run {
                        translationY = -y
                        val bounceAnim = SpringAnimation(welcome_message, DynamicAnimation.TRANSLATION_Y)
                        val spring = SpringForce().apply {
                            finalPosition = 0f
                            stiffness = SpringForce.STIFFNESS_LOW
                            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                        }
                        bounceAnim.spring = spring
                        bounceAnim.start()
                    }

                    sign_up_button.run {
                        translationY = welcome_layout.bottom - y
                        animate()
                                .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                                .translationY(0f)
                    }

                    login_button.run {
                        translationY = welcome_layout.bottom - y
                        animate()
                                .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                                .translationY(0f)
                    }

                    skip_for_now_button.run {
                        translationY = welcome_layout.bottom - y
                        animate()
                                .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
                                .translationY(0f)
                    }
                }
            }
        }

        sign_up_button.setOnClickListener {
            mWelcomePresenter.initSignUp()
        }

        login_button.setOnClickListener {
            mWelcomePresenter.initSignIn()
        }

        skip_for_now_button.setOnClickListener {
            mWelcomePresenter.skipSignIn()
        }

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mInteractionListener = if (context is WelcomeFragmentInteractionListener) context
        else throw RuntimeException("${context.toString()} must implement ${WelcomeFragmentInteractionListener::class.java.simpleName}")
    }

    override fun onDetach() {
        super.onDetach()
        mInteractionListener = null
    }

    override fun showSignUpView() {
        mInteractionListener?.showFireBaseAuthUi()
    }

    override fun showSignInView() {
        mInteractionListener?.showLogInFragment()
    }

    override fun showSkipSignInView() {
        mInteractionListener?.showSkipSignInFragment()
    }

    override fun showHomeView(userRole: UserRole) {
        mInteractionListener?.showHomeActivity(userRole)
    }
}