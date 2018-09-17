package com.allow.food4needy.adddonation

import android.arch.lifecycle.LiveData
import android.location.Location
import android.util.Log
import com.allow.food4needy.common.Event
import com.allow.food4needy.common.getCountryIso
import com.allow.food4needy.common.mGson
import com.allow.food4needy.domain.*
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.util.*

class AddDonationLiveData : LiveData<Event<Response<Donation, String>>>() {

    private val userId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    fun submitDonation(location: Location, foodFrequency: DonationFrequency, foodName: String, foodWeight: String, foodExpiry: String) {
        value = Event(Response.loading())

        mDatabase.child("users").child("all").child("data").child(userId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Get user value
                        val jsonString = mGson.toJson(dataSnapshot.value)
                        val user = mGson.fromJson(jsonString, User::class.java)

                        if (user == null) {
                            Timber.w("User $userId is unexpectedly null")
                            value = Event(Response.error("User $userId is unexpectedly null"))
                        } else {
                            mDatabase.child("user-donations").child(userId).child("state").child("${DonationState.CREATED.ordinal}").child("all").child("count").addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                                            val userCreatedCount = dataSnapshot.value as? Long ?: 0

                                            val key = mDatabase.child("donations").child("all").child("data").push().key
                                            val donation = Donation(
                                                    frequency = foodFrequency,
                                                    userRole = user.role,
                                                    userId = userId,
                                                    user = user.name,
                                                    item = foodName,
                                                    weight = foodWeight,
                                                    expiry = foodExpiry,
                                                    address = user.address
                                            )

                                            key?.let { donationKey ->
                                                HashMap<String, Any>().let { childUpdates ->
                                                    donation.toMap().let { donationMap ->
                                                        childUpdates["donations/all/data/$donationKey"] = donationMap
                                                        childUpdates["donations/state/${DonationState.CREATED.ordinal}/all/data/$donationKey"] = donationMap
                                                        childUpdates["donations/state/${DonationState.CREATED.ordinal}/freq/${donation.frequency.ordinal}/all/data/$donationKey"] = donationMap
                                                        childUpdates["donations/state/${DonationState.CREATED.ordinal}/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = donationMap
                                                        childUpdates["donations/state/${DonationState.CREATED.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = donationMap
                                                        childUpdates["donations/freq/${donation.frequency.ordinal}/all/data/$donationKey"] = donationMap
                                                        childUpdates["donations/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = donationMap
                                                        childUpdates["donations/role/${donation.userRole.ordinal}/data/$donationKey"] = donationMap

                                                        childUpdates["user-donations/$userId/all/data/$donationKey"] = donationMap
                                                        childUpdates["user-donations/$userId/state/${DonationState.CREATED.ordinal}/all/count"] = userCreatedCount + 1
                                                        childUpdates["user-donations/$userId/state/${DonationState.CREATED.ordinal}/all/data/$donationKey"] = donationMap
                                                        childUpdates["user-donations/$userId/state/${DonationState.CREATED.ordinal}/freq/${donation.frequency.ordinal}/data/$donationKey"] = donationMap

                                                        mDatabase.updateChildren(childUpdates).addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {

                                                                val mGeoFire = GeoFire(FirebaseDatabase.getInstance().getReference("donation-location").child(getCountryIso).child("role").child("${user.role.ordinal}"))
                                                                mGeoFire.setLocation(donationKey, GeoLocation(location.latitude, location.longitude)) { _, error ->
                                                                    if (error != null) {
                                                                        Timber.w(error.toException(), "submitDonation:setLocation:unSuccessful")
                                                                        value = Event(Response.error("submitDonation:setLocation:unSuccessful${error.toException()}"))
                                                                    } else {
                                                                        Timber.i("Donation $foodName added")
                                                                        value = Event(Response.success(donation))
                                                                    }
                                                                }

                                                            } else {
                                                                Timber.w(task.exception, "submitDonation:unSuccessful")
                                                                value = Event(Response.error("submitDonation:unSuccessful${task.exception}"))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Timber.w(databaseError.toException(), "submitDonation:getCreatedCount:onCancelled")
                                            value = Event(Response.error("submitDonation:getCreatedCount:onCancelled${databaseError.toException()}"))
                                        }
                                    })

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Timber.w(databaseError.toException(), "getUser:onCancelled")
                        value = Event(Response.error("getUser:onCancelled${databaseError.toException()}"))
                    }
                })
    }
}