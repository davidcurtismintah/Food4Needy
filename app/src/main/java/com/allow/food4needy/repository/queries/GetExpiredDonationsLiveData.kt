package com.allow.food4needy.repository.queries

import android.arch.lifecycle.LiveData
import com.allow.food4needy.domain.DonationState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GetExpiredDonationsLiveData(val onStartAction: () -> Unit, val onFinishAction: () -> Unit, val onSuccessAction: () -> Unit, val onErrorAction: () -> Unit) : LiveData<Long>() {

    private val userId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference }

    init {
        getDonationsSummary()
    }

    fun getDonationsSummary() {
        onStartAction()
        mDatabase.child("user-donations").child(userId).child("state").child("${DonationState.EXPIRED.ordinal}").child("all").child("count").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        value = (dataSnapshot.value as? Long) ?: 0
                        onSuccessAction()
                        onFinishAction()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        onErrorAction()
                        onFinishAction()
                    }
                })
    }
}
