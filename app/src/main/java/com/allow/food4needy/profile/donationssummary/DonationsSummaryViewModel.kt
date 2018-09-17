package com.allow.food4needy.profile.donationssummary

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.allow.food4needy.common.Event
import com.allow.food4needy.repository.queries.GetCreatedDonationsLiveData
import com.allow.food4needy.repository.queries.GetDonatedDonationsLiveData
import com.allow.food4needy.repository.queries.GetExpiredDonationsLiveData

class DonationsSummaryViewModel : ViewModel() {

    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val showSnackBarLiveData = MutableLiveData<Event<Boolean>>()
    val createdDonationsLiveData = GetCreatedDonationsLiveData(
            onStartAction = { loadingLiveData.value = Event(true) },
            onFinishAction = { loadingLiveData.value = Event(false) },
            onSuccessAction = {},
            onErrorAction = { showSnackBarLiveData.value = Event(true) }
    )
    val donatedDonationsLiveData = GetDonatedDonationsLiveData(
            onStartAction = { loadingLiveData.value = Event(true) },
            onFinishAction = { loadingLiveData.value = Event(false) },
            onSuccessAction = {},
            onErrorAction = { showSnackBarLiveData.value = Event(true) }
    )
    val expiredDonationsLiveData = GetExpiredDonationsLiveData(
            onStartAction = { loadingLiveData.value = Event(true) },
            onFinishAction = { loadingLiveData.value = Event(false) },
            onSuccessAction = {},
            onErrorAction = { showSnackBarLiveData.value = Event(true) }
    )
}
