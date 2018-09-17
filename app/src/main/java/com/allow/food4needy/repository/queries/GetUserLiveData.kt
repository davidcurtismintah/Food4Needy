package com.allow.food4needy.repository.queries

import android.arch.lifecycle.LiveData
import com.allow.food4needy.common.mGson
import com.allow.food4needy.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class GetUserLiveData(val onStartAction: () -> Unit, val onFinishAction: () -> Unit, private val onSuccessAction: () -> Unit, val onErrorAction: () -> Unit) : LiveData<User>() {

    private val userId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    init {
        getUser()
    }

    fun getUser() {
        onStartAction()
        mDatabase.child("users").child("all").child("data").child(userId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Get user value
                        val jsonString = mGson.toJson(dataSnapshot.value)
                        val user: User? = mGson.fromJson(jsonString, User::class.java)

                        if (user == null) {
                            Timber.e("User $userId is unexpectedly null")
                            onErrorAction()
                        } else {
                            value = user
                            onSuccessAction()
                        }

                        onFinishAction()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Timber.w("getUser:onCancelled", databaseError.toException())
                        onErrorAction()
                        onFinishAction()
                    }
                })
    }
}
