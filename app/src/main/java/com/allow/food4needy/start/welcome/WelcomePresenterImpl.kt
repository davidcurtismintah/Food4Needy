package com.allow.food4needy.start.welcome

import com.allow.food4needy.domain.UserRole
import com.allow.food4needy.repository.UserManager

class WelcomePresenterImpl(private val view: WelcomeView, val userManager: UserManager) : WelcomePresenter {

    override fun initSignUp() {
        view.showSignUpView()
    }

    override fun initSignIn() {
        view.showSignInView()
    }

    override fun skipSignIn() {
        view.showSkipSignInView()
    }

}