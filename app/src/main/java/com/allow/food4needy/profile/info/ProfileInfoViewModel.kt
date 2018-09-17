package com.allow.food4needy.profile.info

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.allow.food4needy.common.Event
import com.allow.food4needy.domain.User
import com.allow.food4needy.repository.queries.GetUserLiveData

class ProfileInfoViewModel : ViewModel() {

    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val showSnackBarLiveData = MutableLiveData<Event<Boolean>>()
    val userLiveData = GetUserLiveData(
            onStartAction = { loadingLiveData.value = Event(true) },
            onFinishAction = { loadingLiveData.value = Event(false) },
            onSuccessAction = { },
            onErrorAction = { showSnackBarLiveData.value = Event(true) }
    )


    val updateProfileInfoLiveData = UpdateProfileLiveData()

    fun updateProfileInfo(updated: User) {
        updateProfileInfoLiveData.updateProfileInfo(updated)
    }
}
