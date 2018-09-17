package com.allow.food4needy.repository.queries

import android.arch.lifecycle.LiveData
import android.util.Log
import com.allow.food4needy.domain.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class UserRoleQueryLiveData : LiveData<UserRole>() {

    private val userId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    init {
        mDatabase.child("users").child("all").child("data").child(userId).child("role").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val roleValue = dataSnapshot.value as? Long

                        if (roleValue == null) {
                            Timber.e("User role is unexpectedly null")
                        } else {
                            value = UserRole.values()[roleValue.toInt()]
                            Timber.i("User role obtained $roleValue")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Timber.w(databaseError.toException(), "getUserRole:onCancelled")
                    }
                })
    }
}