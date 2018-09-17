package com.allow.food4needy.home.nearbydonations

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.allow.food4needy.Food4NeedyApplication
import com.allow.food4needy.common.getCountryIso
import com.allow.food4needy.domain.Donation
import com.allow.food4needy.domain.DonationState
import com.allow.food4needy.domain.UserRole
import com.allow.food4needy.home.GetNearbyDataLiveData
import com.google.firebase.database.FirebaseDatabase

class NearbyDonationsViewModel(donationUserRole: UserRole) : ViewModel() {

    val requestCount = MutableLiveData<Int>()

    val currentDonation = MutableLiveData<Donation>()

    val pickDonationLiveData = PickDonationLiveData()

    val expireDonationLiveData = ExpireDonationLiveData()

    val getNearbyDonationsLiveData = GetNearbyDataLiveData.donations(
            Food4NeedyApplication.instance,
            keyRef = FirebaseDatabase.getInstance().reference.child("donation-location").child(getCountryIso).child("role").child("${donationUserRole.ordinal}"),
            dataRef = FirebaseDatabase.getInstance().reference.child("donations").child("state").child("${DonationState.CREATED.ordinal}").child("all").child("data")
    )

}