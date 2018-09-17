package com.allow.food4needy.home.mydonations

import android.arch.lifecycle.LiveData
import android.util.Log
import com.allow.food4needy.common.Event
import com.allow.food4needy.common.getCountryIso
import com.allow.food4needy.common.mGson
import com.allow.food4needy.domain.Donation
import com.allow.food4needy.domain.DonationState
import com.allow.food4needy.domain.Response
import com.allow.food4needy.domain.User
import com.firebase.geofire.GeoFire
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.util.*

class DonateDonationLiveData : LiveData<Event<Response<Donation, String>>>() {

    private val userId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    fun donateDonation(donation: Donation) {
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

                                            mDatabase.child("user-donations").child(userId).child("state").child("${DonationState.DONATED.ordinal}").child("all").child("count").addListenerForSingleValueEvent(
                                                    object : ValueEventListener {
                                                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                                                            val userDonatedCount = dataSnapshot.value as? Long ?: 0

                                                            donation.state = DonationState.DONATED

                                                            donation.id.let { donationKey ->
                                                                HashMap<String, Any?>().let { childUpdates ->
                                                                    donation.toMap().let { donationMap ->
                                                                        childUpdates["donations/all/data/$donationKey"] = donationMap
                                                                        childUpdates["donations/state/${DonationState.CREATED.ordinal}/all/data/$donationKey"] = null
                                                                        childUpdates["donations/state/${DonationState.CREATED.ordinal}/freq/${donation.frequency.ordinal}/all/data/$donationKey"] = null
                                                                        childUpdates["donations/state/${DonationState.CREATED.ordinal}/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = null
                                                                        childUpdates["donations/state/${DonationState.CREATED.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = null
                                                                        childUpdates["donations/state/${DonationState.DONATED.ordinal}/all/data/$donationKey"] = donationMap
                                                                        childUpdates["donations/state/${DonationState.DONATED.ordinal}/freq/${donation.frequency.ordinal}/all/data/$donationKey"] = donationMap
                                                                        childUpdates["donations/state/${DonationState.DONATED.ordinal}/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = donationMap
                                                                        childUpdates["donations/state/${DonationState.DONATED.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = donationMap
                                                                        childUpdates["donations/freq/${donation.frequency.ordinal}/all/data/$donationKey"] = donationMap
                                                                        childUpdates["donations/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = donationMap
                                                                        childUpdates["donations/role/${donation.userRole.ordinal}/data/$donationKey"] = donationMap

                                                                        childUpdates["user-donations/$userId/all/data/$donationKey"] = donationMap
                                                                        childUpdates["user-donations/$userId/state/${DonationState.CREATED.ordinal}/all/count"] = if (userCreatedCount > 0) userCreatedCount - 1 else null
                                                                        childUpdates["user-donations/$userId/state/${DonationState.CREATED.ordinal}/all/data/$donationKey"] = null
                                                                        childUpdates["user-donations/$userId/state/${DonationState.CREATED.ordinal}/freq/${donation.frequency.ordinal}/data/$donationKey"] = null
                                                                        childUpdates["user-donations/$userId/state/${DonationState.DONATED.ordinal}/all/count"] = if (userDonatedCount > 0) userDonatedCount - 1 else null
                                                                        childUpdates["user-donations/$userId/state/${DonationState.DONATED.ordinal}/all/data/$donationKey"] = donationMap
                                                                        childUpdates["user-donations/$userId/state/${DonationState.DONATED.ordinal}/freq/${donation.frequency.ordinal}/data/$donationKey"] = donationMap

                                                                        mDatabase.updateChildren(childUpdates).addOnCompleteListener { task ->
                                                                            if (task.isSuccessful) {
                                                                                val mGeoFire = GeoFire(FirebaseDatabase.getInstance().getReference("donation-location").child(getCountryIso).child("role").child("${user.role.ordinal}"))
                                                                                mGeoFire.removeLocation(donationKey) { _, error ->
                                                                                    if (error != null) {
                                                                                        Timber.w(error.toException(), "donateDonation:removeLocation:unSuccessful")
                                                                                        value = Event(Response.error("donateDonation:removeLocation:unSuccessful${error.toException()}"))
                                                                                    } else {
                                                                                        Timber.i("Donation ${donation.item} donated")
                                                                                        value = Event(Response.success(donation))
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                Timber.w(task.exception, "donateDonation:unSuccessful")
                                                                                value = Event(Response.error("donateDonation:unSuccessful${task.exception}"))
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(databaseError: DatabaseError) {
                                                            Timber.w(databaseError.toException(), "donateDonation:getDonatedCount:onCancelled")
                                                            value = Event(Response.error("donateDonation:getDonatedCount:onCancelled${databaseError.toException()}"))
                                                        }
                                                    })
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Timber.w(databaseError.toException(), "donateDonation:getCreatedCount:onCancelled")
                                            value = Event(Response.error("donateDonation:getCreatedCount:onCancelled${databaseError.toException()}"))
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