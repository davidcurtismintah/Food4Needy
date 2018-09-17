package com.allow.food4needy.home.mydonations

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import com.allow.food4needy.domain.Donation

class MyDonationsViewModel : ViewModel() {

    val currentDonation = MutableLiveData<Donation>()

//    val listIsEmptyLiveData = MutableLiveData<Event<Boolean>>()
//    val recyclerAdapterLiveData = QueryRecyclerAdapterLiveData(
//            onDataChangedAction = { itemCount ->
//                listIsEmptyLiveData.value = Event(itemCount == 0)
//                Timber.d("MyDonationsViewModel... emptyListAction: $itemCount")
//            },
//            actionOnDonate = {
//                donateDonationLiveData.donateDonation(it)
//            },
//            actionOnDelete = {
//                deleteConfirmationLiveData.value = Event(it)
//            },
//            actionOnExpire = {
//                expireDonationLiveData.expireDonation(it)
//            }
//    )

    val donateDonationLiveData = DonateDonationLiveData()

//    val deleteConfirmationLiveData = MutableLiveData<Event<Donation>>()
    val deleteDonationLiveData = DeleteDonationLiveData()

    val expireDonationLiveData = ExpireDonationLiveData()

    val postSelfieLiveData = PostSelfieLiveData()
//    fun makePost(imageBitmap: Bitmap) {
//        postSelfieLiveData.makePost(imageBitmap)
//    }

}
