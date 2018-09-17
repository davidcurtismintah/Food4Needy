package com.allow.food4needy.domain

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ServerValue
import java.util.*

@IgnoreExtraProperties
data class User(
        override var id: String = "",
        var created: Long = 0,
        val role: UserRole = UserRole.GUEST,
        val name: String = "",
        val imageUrl: String = "",
        val phone: String = "",
        val alternatePhone: String = "",
        val dateOfBirth: String = "",
        val email: String = "",
        val address: String = "",
        val country: String = "",
        override var latitude: Double = 0.0,
        override var longitude: Double = 0.0
) : NearbyData {

    @Exclude
    fun toMap() = HashMap<String, Any>().apply {
        this["created"] = ServerValue.TIMESTAMP
        this["role"] = role.ordinal
        this["name"] = name
        this["imageUrl"] = imageUrl
        this["phone"] = phone
        this["alternatePhone"] = alternatePhone
        this["dateOfBirth"] = dateOfBirth
        this["email"] = email
        this["address"] = address
        this["country"] = country
    }
}