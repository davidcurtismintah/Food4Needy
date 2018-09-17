package com.allow.food4needy.home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.allow.food4needy.repository.queries.UserImageQueryLiveData
import com.allow.food4needy.repository.queries.UserNameQueryLiveData
import com.allow.food4needy.repository.queries.UserRoleQueryLiveData

class HomeViewModel : ViewModel(){

    val userRoleLiveData = UserRoleQueryLiveData()
    val userImageLiveData = UserImageQueryLiveData()
    val userNameLiveData = UserNameQueryLiveData()
    val listBottomPadding = MutableLiveData<Int>()
}