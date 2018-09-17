package com.allow.food4needy.home.nearbyvolunteers

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.allow.food4needy.common.getCountryIso
import com.allow.food4needy.domain.DonationState
import com.allow.food4needy.domain.User
import com.allow.food4needy.domain.UserRole
import com.allow.food4needy.home.GetNearbyDataLiveData
import com.google.firebase.database.FirebaseDatabase

class NearbyVolunteersViewModel(application: Application) : AndroidViewModel(application) {

    val currentVolunteer = MutableLiveData<User>()


    val getNearbyVolunteersLiveData = GetNearbyDataLiveData.volunteers(
            application,
            keyRef = FirebaseDatabase.getInstance().reference.child("user-location").child(getCountryIso).child("role").child("${UserRole.VOLUNTEER.ordinal}"),
            dataRef = FirebaseDatabase.getInstance().reference.child("users").child("role").child("${UserRole.VOLUNTEER.ordinal}").child("data")
    )
}