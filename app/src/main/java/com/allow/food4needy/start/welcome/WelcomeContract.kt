package com.allow.food4needy.start.welcome

import com.allow.food4needy.domain.UserRole

interface WelcomeView {
    fun showSignUpView()
    fun showSignInView()
    fun showSkipSignInView()
    fun showHomeView(userRole: UserRole)
}

interface WelcomePresenter {
    fun initSignUp()
    fun initSignIn()
    fun skipSignIn()
}