package com.allow.food4needy.common

import com.allow.food4needy.domain.DonationFrequency
import com.allow.food4needy.domain.DonationState
import com.allow.food4needy.domain.UserRole
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

private val mDonationFrequencyTypeAdapter by lazy {
    object : JsonSerializer<DonationFrequency>, JsonDeserializer<DonationFrequency> {
        override fun serialize(src: DonationFrequency?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? = JsonPrimitive(src?.ordinal)

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DonationFrequency? {
            if (json?.asString == null || json.asString.isEmpty()) {
                return null
            }
            return DonationFrequency.values()[(json.asString.toInt())]
        }
    }
}

private val mUserRoleTypeAdapter by lazy {
    object : JsonSerializer<UserRole>, JsonDeserializer<UserRole> {
        override fun serialize(src: UserRole?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? = JsonPrimitive(src?.ordinal)

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): UserRole? {
            if (json?.asString == null || json.asString.isEmpty()) {
                return null
            }
            return UserRole.values()[(json.asString.toInt())]
        }
    }
}

private val mDonationStateTypeAdapter by lazy {
    object : JsonSerializer<DonationState>, JsonDeserializer<DonationState> {
        override fun serialize(src: DonationState?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? = JsonPrimitive(src?.ordinal)

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DonationState? {
            if (json?.asString == null || json.asString.isEmpty()) {
                return null
            }
            return DonationState.values()[(json.asString.toInt())]
        }
    }
}

val mGson: Gson by lazy {
    GsonBuilder()
            .registerTypeAdapter(DonationState::class.java, mDonationStateTypeAdapter)
            .registerTypeAdapter(DonationFrequency::class.java, mDonationFrequencyTypeAdapter)
            .registerTypeAdapter(UserRole::class.java, mUserRoleTypeAdapter)
            .create()
}

//inline fun <reified T: Any> Gson.fromJson(json: String): T = this.fromJson(json, T::class.java)

//inline fun <T: Any> Gson.fromJsonGeneric(json: String): T = this.fromJson(json, object : TypeToken<T>() {}.type)
