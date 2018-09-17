package com.allow.food4needy.start.adduser

import com.allow.food4needy.domain.User
import com.allow.food4needy.domain.UserRole

interface AddUserView {
    fun setUpViews(user: User)
    fun showHomeView(userRole: UserRole)
    fun showLoading(loading: Boolean)
}

interface AddUserPresenter {
    val userRole: UserRole
    fun start()
    fun updateUserDetails(user: User)
}