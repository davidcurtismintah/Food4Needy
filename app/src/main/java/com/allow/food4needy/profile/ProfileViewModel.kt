package com.allow.food4needy.profile

import android.arch.lifecycle.ViewModel
import com.allow.food4needy.repository.queries.UserImageQueryLiveData
import com.allow.food4needy.repository.queries.UserNameQueryLiveData
import com.allow.food4needy.repository.queries.UserRoleQueryLiveData

class ProfileViewModel : ViewModel(){

    val userRoleLiveData = UserRoleQueryLiveData()
    val userImageLiveData = UserImageQueryLiveData()
    val userNameLiveData = UserNameQueryLiveData()
}
