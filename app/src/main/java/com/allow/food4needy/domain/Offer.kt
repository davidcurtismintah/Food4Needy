package com.allow.food4needy.domain

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue
import java.util.*

class Offer(
        var id: String = "",
        var frequency: DonationFrequency = DonationFrequency.DAILY,
        var created: Long = 0,
        var restaurantId: String = "",
        var restaurant: String = "",
        var volunteerId: String = "",
        var volunteer: String = "",
        var imageUrl: String = "",
        var item: String = "",
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var address:  String = ""
) {
    @Exclude
    fun toMap() = HashMap<String, Any>().apply {
        this["frequency"] = frequency.ordinal
        this["created"] = ServerValue.TIMESTAMP
        this["restaurantId"] = restaurantId
        this["restaurant"] = restaurant
        this["volunteerId"] = volunteerId
        this["volunteer"] = volunteer
        this["imageUrl"] = imageUrl
        this["item"] = item
        this["address"] = address
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Donation

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}