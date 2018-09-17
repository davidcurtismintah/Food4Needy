package com.allow.food4needy.start.selectrole

import com.allow.food4needy.domain.UserRole

class SelectRolePresenterImpl(private val view: SelectRoleView) : SelectRolePresenter {
    override fun setUpUserDetailsFor(userRole: UserRole) {
        view.showUserDetailsView(userRole)
    }
}