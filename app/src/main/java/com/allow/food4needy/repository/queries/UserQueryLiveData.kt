package com.allow.food4needy.repository.queries

import android.arch.lifecycle.LiveData
import android.util.Log
import com.allow.food4needy.common.mGson
import com.allow.food4needy.domain.Response
import com.allow.food4needy.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class UserQueryLiveData : LiveData<Response<User, String>>() {

    private val userId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    init {
        value = Response.loading()
        mDatabase.child("users").child("all").child("data").child(userId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Get user value
                        val jsonString = mGson.toJson(dataSnapshot.value)
                        val user = mGson.fromJson(jsonString, User::class.java)

                        if (user == null) {
                            Timber.w("User $userId is unexpectedly null")
                            value = Response.error("User $userId is unexpectedly null")
                        } else {
                            value = Response.success(user)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Timber.w(databaseError.toException(), "getUser:onCancelled")
                        value = Response.error("getUser:onCancelled${databaseError.toException()}")
                    }
                })
    }
}