package com.allow.food4needy.home.mydonations

import android.arch.lifecycle.LiveData
import android.util.Log
import com.allow.food4needy.common.Event
import com.allow.food4needy.common.getCountryIso
import com.allow.food4needy.common.mGson
import com.allow.food4needy.domain.Donation
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

class DeleteDonationLiveData : LiveData<Event<Response<String, String>>>() {

    private val userId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    fun deleteDonation(donation: Donation) {
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
                            mDatabase.child("user-donations").child(userId).child("state").child("${donation.state.ordinal}").child("all").child("count").addListenerForSingleValueEvent(
                                    object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                                            val userDonationStateCount = dataSnapshot.value as? Long ?: 0

                                            mDatabase.child("volunteer-donations-picked").child(donation.volunteerId).child("all").child("count").addListenerForSingleValueEvent(
                                                    object : ValueEventListener {
                                                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                                                            val volunteerDonationsPickedCount = dataSnapshot.value as? Long ?: 0

                                                            donation.id.let { donationKey ->
                                                                HashMap<String, Any?>().let { childUpdates ->
                                                                    donation.toMap().let { _ ->
                                                                        childUpdates["donations/all/data/$donationKey"] = null
                                                                        childUpdates["donations/state/${donation.state.ordinal}/all/data/$donationKey"] = null
                                                                        childUpdates["donations/state/${donation.state.ordinal}/freq/${donation.frequency.ordinal}/all/data/$donationKey"] = null
                                                                        childUpdates["donations/state/${donation.state.ordinal}/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = null
                                                                        childUpdates["donations/state/${donation.state.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = null
                                                                        childUpdates["donations/freq/${donation.frequency.ordinal}/all/data/$donationKey"] = null
                                                                        childUpdates["donations/freq/${donation.frequency.ordinal}/role/${donation.userRole.ordinal}/data/$donationKey"] = null
                                                                        childUpdates["donations/role/${donation.userRole.ordinal}/data/$donationKey"] = null

                                                                        childUpdates["user-donations/$userId/all/data/$donationKey"] = null
                                                                        childUpdates["user-donations/$userId/state/${donation.state.ordinal}/all/count"] = if (userDonationStateCount > 0) userDonationStateCount - 1 else null
                                                                        childUpdates["user-donations/$userId/state/${donation.state.ordinal}/all/data/$donationKey"] = null
                                                                        childUpdates["user-donations/$userId/state/${donation.state.ordinal}/freq/${donation.frequency.ordinal}/data/$donationKey"] = null


                                                                        childUpdates["volunteer-donations-picked/${donation.volunteerId}/all/$donationKey"] = null
                                                                        childUpdates["volunteer-donations-picked/${donation.volunteerId}/count"] = if (volunteerDonationsPickedCount > 0) volunteerDonationsPickedCount - 1 else null

                                                                        mDatabase.updateChildren(childUpdates).addOnCompleteListener { task ->
                                                                            if (task.isSuccessful) {
                                                                                val mGeoFire = GeoFire(FirebaseDatabase.getInstance().getReference("donation-location").child(getCountryIso).child("role").child("${user.role.ordinal}"))
                                                                                mGeoFire.removeLocation(donationKey) { _, error ->
                                                                                    if (error != null) {
                                                                                        Timber.w(error.toException(), "deleteDonation:removeLocation:unSuccessful")
                                                                                        value = Event(Response.error("deleteDonation:removeLocation:unSuccessful${error.toException()}"))
                                                                                    } else {
                                                                                        Timber.i("Donation: ${donation.item} deleted")
                                                                                        value = Event(Response.success(donation.item))
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                Timber.w(task.exception, "deleteDonation:unSuccessful")
                                                                                value = Event(Response.error("deleteDonation:unSuccessful${task.exception}"))
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(databaseError: DatabaseError) {
                                                            Timber.w(databaseError.toException(), "deleteDonation:volunteerDonationsPickedCount:onCancelled")
                                                            value = Event(Response.error("deleteDonation:volunteerDonationsPickedCount:onCancelled${databaseError.toException()}"))
                                                        }
                                                    })
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Timber.w(databaseError.toException(), "deleteDonation:userDonationStateCount:onCancelled")
                                            value = Event(Response.error("deleteDonation:userDonationStateCount:onCancelled${databaseError.toException()}"))
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