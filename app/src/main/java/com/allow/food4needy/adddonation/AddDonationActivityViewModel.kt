package com.allow.food4needy.adddonation

import android.arch.lifecycle.ViewModel
import com.allow.food4needy.repository.queries.UserRoleQueryLiveData

class AddDonationActivityViewModel : ViewModel() {

    val userRoleLiveData = UserRoleQueryLiveData()

}