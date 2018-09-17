package com.allow.food4needy.repository

import com.allow.food4needy.Food4NeedyApplication
import com.allow.food4needy.domain.User
import com.allow.food4needy.domain.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


object FirebaseUserManager : UserManager {

    private const val USER_ID = "user_id"
    private const val USER_LOGGED_IN = "user_logged_in"
    private const val USER_ROLE = "user_role"
    private const val USER_NAME = "user_name"
    private const val USER_EMAIL = "user_email"
    private const val USER_PHONE = "user_phone"
    private const val USER_ALTERNATIVE_PHONE = "user_alternative_phone"
    private const val USER_ADDRESS = "user_address"

    private val mAuth by lazy { FirebaseAuth.getInstance()!! }
    private val mDatabase by lazy { Food4NeedyApplication.fdb.reference }
    private val mPrefs by lazy { Food4NeedyApplication.prefs }

//    override val userLoggedIn: Boolean
//        get() = mAuth.currentUser != null
    override val userLoggedIn: Boolean
        get() = mPrefs.getBoolean(USER_LOGGED_IN, false)
    override val userId: String
        get() = mAuth.currentUser?.uid ?: ""
    override val userRole: Int
        get() = mPrefs.getInt(USER_ROLE, UserRole.GUEST.ordinal)
    override val userImageUrl: String
        get() = mAuth.currentUser?.photoUrl?.path ?: ""
    override val userName: String
        get() = mAuth.currentUser?.displayName ?: ""
    override val userEmail: String
        get() = mAuth.currentUser?.email ?: ""
    override val userPhone: String
        get() = mAuth.currentUser?.phoneNumber ?: ""

    override val userAlternatePhone: String
        get() = mPrefs.getString(USER_ALTERNATIVE_PHONE, "")

    override val userAddress: String
        get() = mPrefs.getString(USER_ADDRESS, "")

    override val currentUser: User
        get() = User(
                id = userId,
                role = UserRole.values()[userRole],
                name = userName,
                address = userAddress,
                country = "",
                phone = userPhone,
                dateOfBirth = "",
                email = userEmail
        )

    override fun getCurrentUser(actionOnSuccess: (User) -> Unit, actionOnError: () -> Unit) {
        val userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java) ?: User()
                with(mPrefs.edit()) {
                    putBoolean(USER_LOGGED_IN, true)
                    putString(USER_ID, user.id)
                    putInt(USER_ROLE, user.role.ordinal)
                    putString(USER_NAME, user.name)
                    putString(USER_EMAIL, user.email)
                    putString(USER_PHONE, user.phone)
                    putString(USER_ALTERNATIVE_PHONE, user.alternatePhone)
                    putString(USER_ADDRESS, user.address)
                    apply()
                }
                actionOnSuccess(user)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                actionOnError()
            }
        }
        mAuth.currentUser?.uid?.let {
            mDatabase.child("users").child("all").child("data").child(it).addListenerForSingleValueEvent(userListener)
        }
    }

    override fun getUserById(userId: String, actionOnSuccess: (User) -> Unit, actionOnError: () -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addUser(user: User, actionOnSuccess: () -> Unit, actionOnError: () -> Unit) {
        mAuth.currentUser?.uid?.let { userId ->
            HashMap<String, Any>().let { childUpdates ->
                user.toMap().let { userMap ->
                    childUpdates["users/all/data/$userId"] = userMap
                    childUpdates["users/role/${user.role.ordinal}/data/$userId"] = userMap
                    mDatabase.updateChildren(childUpdates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            with(mPrefs.edit()) {
                                putBoolean(USER_LOGGED_IN, true)
                                putString(USER_ID, user.id)
                                putInt(USER_ROLE, user.role.ordinal)
                                putString(USER_NAME, user.name)
                                putString(USER_EMAIL, user.email)
                                putString(USER_PHONE, user.phone)
                                putString(USER_ALTERNATIVE_PHONE, user.alternatePhone)
                                putString(USER_ADDRESS, user.address)
                                apply()
                            }
                            actionOnSuccess()
                        } else {
                            actionOnError()
                        }
                    }
                }
            }
        }
    }

    override fun skipSignIn(actionOnSuccess: () -> Unit, actionOnError: () -> Unit) {
        mAuth.currentUser?.uid?.let { userId ->
            with(mPrefs.edit()) {
                putBoolean(USER_LOGGED_IN, true)
                putString(USER_ID, userId)
                putInt(USER_ROLE, UserRole.GUEST.ordinal)
                putString(USER_NAME, "")
                putString(USER_EMAIL, "")
                putString(USER_PHONE, "")
                putString(USER_ALTERNATIVE_PHONE, "")
                putString(USER_ADDRESS, "")
                apply()
            }
            actionOnSuccess()
        }
    }
}