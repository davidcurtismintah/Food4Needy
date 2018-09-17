package com.allow.food4needy.home.nearbydonations

import android.arch.lifecycle.LiveData
import android.util.Log
import com.allow.food4needy.common.Event
import com.allow.food4needy.common.getCountryIso
import com.allow.food4needy.common.mGson
import com.allow.food4needy.domain.*
import com.firebase.geofire.GeoFire
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.*
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

class PickDonationLiveData : LiveData<Event<Response<Donation, String>>>() {

    private val userId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    fun pickUpDonation(donation: Donation) {

        if (donation.volunteerId.isNotEmpty()) {
            value = Event(Response.success(donation))
            return
        }

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
                            mDatabase.child("volunteer-donations-picked").child(userId).child("all").child("count").addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                                            val volunteerDonationsPickedCount = dataSnapshot.value as? Long ?: 0

                                            donation.volunteer = user.name
                                            donation.volunteerId = userId

                                            donation.id.let { donationKey ->
                                                HashMap<String, Any?>().let { childUpdates ->
                                                    donation.toMap().let { donationMap ->
                                                        childUpdates["donations/all/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["donations/all/data/$donationKey/volunteer"] = user.name
                                                        childUpdates["donations/state/${donation.state.ordinal}/all/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["donations/state/${donation.state.ordinal}/all/data/$donationKey/volunteer"] = user.name
                                                        childUpdates["donations/state/${donation.state.ordinal}/freq/${donation.frequency.ordinal}/all/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["donations/state/${donation.state.ordinal}/freq/${donation.frequency.ordinal}/all/data/$donationKey/volunteer"] = user.name
                                                        childUpdates["donations/state/${donation.state.ordinal}/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["donations/state/${donation.state.ordinal}/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey/volunteer"] = user.name
                                                        childUpdates["donations/state/${donation.state.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["donations/state/${donation.state.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey/volunteer"] = user.name
                                                        
                                                        childUpdates["donations/freq/${donation.frequency.ordinal}/all/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["donations/freq/${donation.frequency.ordinal}/all/data/$donationKey/volunteer"] = user.name
                                                        childUpdates["donations/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["donations/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey/volunteer"] = user.name
                                                        childUpdates["donations/role/${donation.userRole.ordinal}/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["donations/role/${donation.userRole.ordinal}/data/$donationKey/volunteer"] = user.name

                                                        childUpdates["user-donations/${donation.userId}/all/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["user-donations/${donation.userId}/all/data/$donationKey/volunteer"] = user.name
                                                        childUpdates["user-donations/${donation.userId}/state/${donation.state.ordinal}/all/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["user-donations/${donation.userId}/state/${donation.state.ordinal}/all/data/$donationKey/volunteer"] = user.name
                                                        childUpdates["user-donations/${donation.userId}/state/${donation.state.ordinal}/freq/${donation.frequency.ordinal}/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["user-donations/${donation.userId}/state/${donation.state.ordinal}/freq/${donation.frequency.ordinal}/data/$donationKey/volunteer"] = user.name
                                                        childUpdates["user-donations/${donation.userId}/state/${DonationState.DONATED.ordinal}/all/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["user-donations/${donation.userId}/state/${DonationState.DONATED.ordinal}/freq/${donation.frequency.ordinal}/data/$donationKey/volunteerId"] = userId
                                                        childUpdates["user-donations/${donation.userId}/state/${DonationState.DONATED.ordinal}/freq/${donation.frequency.ordinal}/data/$donationKey/volunteer"] = user.name

                                                        childUpdates["volunteer-donations-picked/$userId/all/data/$donationKey"] = donationMap
                                                        childUpdates["volunteer-donations-picked/$userId/all/count"] = volunteerDonationsPickedCount + 1

                                                        mDatabase.updateChildren(childUpdates).addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                Timber.i("Donation ${donation.item} picked")
                                                                value = Event(Response.success(donation))
                                                            } else {
                                                                Timber.w(task.exception, "pickDonation:unSuccessful")
                                                                value = Event(Response.error("pickDonation:unSuccessful${task.exception}"))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Timber.w(databaseError.toException(), "pickDonation:getDonationsPickedCount:onCancelled")
                                            value = Event(Response.error("donateDonation:getDonationsPickedCount:onCancelled${databaseError.toException()}"))
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