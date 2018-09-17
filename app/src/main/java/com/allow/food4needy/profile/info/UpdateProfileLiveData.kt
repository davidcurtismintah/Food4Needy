package com.allow.food4needy.profile.info

import android.arch.lifecycle.LiveData
import android.util.Log
import com.allow.food4needy.common.Event
import com.allow.food4needy.common.mGson
import com.allow.food4needy.domain.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.util.*

class UpdateProfileLiveData : LiveData<Event<Response<User, String>>>() {

    private val userId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    fun updateProfileInfo(updated: User) {
        value = Event(Response.loading())

        mDatabase.child("users").child("all").child("data").child(userId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (!dataSnapshot.exists()) {
                            Timber.w("User $userId does not exist")
                            value = Event(Response.error("User $userId does not exist"))
                        } else {
                            HashMap<String, Any>().let { childUpdates ->
                                updated.toMap().let { userMap ->
                                    childUpdates["users/all/data/$userId"] = userMap
                                    childUpdates["users/role/${updated.role.ordinal}/data/$userId"] = userMap
                                    mDatabase.updateChildren(childUpdates).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Timber.i("User ${updated.name} updated")
                                            value = Event(Response.success(updated))
                                        } else {
                                            Timber.w(task.exception, "updateProfileInfo:unSuccessful")
                                            value = Event(Response.error("updateProfileInfo:unSuccessful${task.exception}"))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Timber.w(databaseError.toException(), "getUser:onCancelled")
                        value = Event(Response.error("getUser:onCancelled${databaseError.toException()}"))
                    }
                })
    }
}