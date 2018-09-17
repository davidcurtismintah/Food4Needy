package com.allow.food4needy.repository.queries

import android.arch.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class UserNameQueryLiveData : LiveData<String>() {

    private val userId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    init {
        mDatabase.child("users").child("all").child("data").child(userId).child("name").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val userName = dataSnapshot.value as? String

                        if (userName == null) {
                            Timber.e("User name is unexpectedly null")
                        } else {
                            value = userName
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Timber.w(databaseError.toException(), "getUsername:onCancelled")
                    }
                })
    }
}