package com.allow.food4needy.start.selectrole

import com.allow.food4needy.domain.UserRole

interface SelectRolePresenter {
    fun setUpUserDetailsFor(userRole: UserRole)
}

interface SelectRoleView {
    fun showUserDetailsView(userRole: UserRole)
}