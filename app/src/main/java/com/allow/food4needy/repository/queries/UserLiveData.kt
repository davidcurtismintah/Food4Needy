package com.allow.food4needy.repository.queries

import android.arch.lifecycle.LiveData
import com.allow.food4needy.common.Event
import com.allow.food4needy.common.mGson
import com.allow.food4needy.domain.Response
import com.allow.food4needy.domain.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class UserLiveData : LiveData<Event<Response<User, String>>>() {

    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    fun getUser(userId: String) {
        value = Event(Response.loading())
        mDatabase.child("users").child("all").child("data").child(userId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Get user value
                        val jsonString = mGson.toJson(dataSnapshot.value)
                        val user: User? = mGson.fromJson(jsonString, User::class.java)

                        if (user == null) {
                            Timber.e("User $userId is unexpectedly null")
                            value = Event(Response.error("User $userId is unexpectedly null"))
                        } else {
                            value = Event(Response.success(user))
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Timber.w(databaseError.toException(), "getUser:onCancelled")
                        value = Event(Response.error("getUser:onCancelled${databaseError.toException()}"))
                    }
                })
    }
}
