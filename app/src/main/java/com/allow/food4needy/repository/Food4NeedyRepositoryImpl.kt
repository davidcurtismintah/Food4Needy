package com.allow.food4needy.repository

import com.allow.food4needy.domain.Donation
import com.allow.food4needy.domain.User

object Food4NeedyRepositoryImpl : Food4NeedyRepository {

    override fun getAllDonations() = TODO()

    override fun getAllDonations(donorId: String) = TODO()

    override fun getExistingDonations(): List<Donation> = TODO()

    override fun getExistingDonations(donorId: String): List<Donation> = TODO()

    override fun getDonatedDonations(): List<Donation> = TODO()

    override fun getDonatedDonations(donorId: String): List<Donation> = TODO()

    override fun getExpiredDonations(): List<Donation> = TODO()

    override fun getExpiredDonations(donorId: String): List<Donation> = TODO()

    override fun saveDonation(donation: Donation) {
        TODO()
    }

    override fun getAllVolunteers(): List<User> {
        TODO()
    }

    override fun getAllRestaurants(): List<User> {
        TODO()
    }
}