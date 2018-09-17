package com.allow.food4needy.start.adduser

import com.allow.food4needy.domain.User
import com.allow.food4needy.domain.UserRole
import com.allow.food4needy.repository.UserManager

class AddUserPresenterImpl(val viewAdd: AddUserView, override val userRole: UserRole, val userManager: UserManager) : AddUserPresenter {

    override fun start() {
        viewAdd.showLoading(false)
        val user = User(
                role = userRole,
                imageUrl = "",
                name = userManager.userName,
                email = userManager.userEmail,
                phone = userManager.userPhone,
                alternatePhone = "",
                address = ""
        )
        viewAdd.setUpViews(user)
    }

    override fun updateUserDetails(user: User) {
        viewAdd.showLoading(true)
        userManager.addUser(
                user = user,
                actionOnSuccess = { viewAdd.showHomeView(userRole) },
                actionOnError = { viewAdd.showLoading(false) }
        )
    }
}
