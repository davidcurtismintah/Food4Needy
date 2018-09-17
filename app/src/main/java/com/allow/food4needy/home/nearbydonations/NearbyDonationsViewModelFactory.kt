package com.allow.food4needy.home.nearbydonations

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.allow.food4needy.domain.UserRole

class NearbyDonationsViewModelFactory(private val donationUserRole: UserRole) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NearbyDonationsViewModel::class.java)) {
            return NearbyDonationsViewModel(donationUserRole) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
