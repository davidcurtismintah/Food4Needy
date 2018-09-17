package com.allow.food4needy.start

import android.arch.lifecycle.ViewModel
import com.allow.food4needy.repository.queries.UserQueryLiveData

class StartViewModel : ViewModel(){

    val userLiveData = UserQueryLiveData()
}
