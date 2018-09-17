package com.allow.food4needy.repository

import com.allow.food4needy.domain.Donation
import com.allow.food4needy.domain.User

interface Food4NeedyRepository {

    fun getAllDonations(): List<Donation>
    fun getExistingDonations(): List<Donation>
    fun getDonatedDonations(): List<Donation>
    fun getExpiredDonations(): List<Donation>
    fun getAllDonations(donorId: String): List<Donation>
    fun getExistingDonations(donorId: String): List<Donation>
    fun getDonatedDonations(donorId: String): List<Donation>
    fun getExpiredDonations(donorId: String): List<Donation>
    fun saveDonation(donation: Donation)

    fun getAllVolunteers(): List<User>

    fun getAllRestaurants(): List<User>
}