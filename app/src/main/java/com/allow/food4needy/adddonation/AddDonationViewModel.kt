package com.allow.food4needy.adddonation

import android.arch.lifecycle.ViewModel
import android.location.Location
import com.allow.food4needy.domain.DonationFrequency

class AddDonationViewModel : ViewModel() {

//    val loadingLiveData = MutableLiveData<Event<Boolean>>()
//    val showSnackBarLiveData = MutableLiveData<Event<Boolean>>()
//    val finishLiveData = MutableLiveData<Event<Boolean>>()

//    private val addDonationLiveData = AddDonationLiveData(
//            onStartAction = { loadingLiveData.value = Event(true) },
//            onFinishAction = { finishLiveData.value = Event(true) },
//            onSuccessAction = { showSnackBarLiveData.value = Event(true) },
//            onErrorAction = { showSnackBarLiveData.value = Event(false) }
//    )

    val addDonationLiveData = AddDonationLiveData()

    fun submitDonation(location: Location, foodFrequency: DonationFrequency, foodName: String, foodWeight: String = "", foodExpiry: String = "") {
        addDonationLiveData.submitDonation(location, foodFrequency, foodName, foodWeight, foodExpiry)
    }
}