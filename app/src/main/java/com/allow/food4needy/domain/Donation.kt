package com.allow.food4needy.domain

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ServerValue
import java.util.*

@IgnoreExtraProperties
data class Donation(
        override var id: String = "",
        var created: Long = 0,
        var frequency: DonationFrequency = DonationFrequency.ONETIME,
        var userRole: UserRole = UserRole.DONOR,
        var userId: String = "",
        var user: String = "",
        var volunteerId: String = "",
        var volunteer: String = "",
        var state: DonationState = DonationState.CREATED,
        var item: String = "",
        var imageUrl: String = "",
        var weight: String = "",
        var expiry: String = "",
        var address: String = "",
        override var latitude: Double = 0.0,
        override var longitude: Double = 0.0
) : NearbyData {

    @Exclude
    fun toMap() = HashMap<String, Any>().apply {
        this["created"] = ServerValue.TIMESTAMP
        this["frequency"] = frequency.ordinal
        this["userRole"] = userRole.ordinal
        this["userId"] = userId
        this["user"] = user
        this["volunteerId"] = volunteerId
        this["volunteer"] = volunteer
        this["state"] = state.ordinal
        this["item"] = item
        this["imageUrl"] = imageUrl
        this["weight"] = weight
        this["expiry"] = expiry
        this["address"] = address
    }

//    fun isSameItem(other: Donation) = id == other.id
//
//    fun hasSameContent(other: Donation) = this == other

}
