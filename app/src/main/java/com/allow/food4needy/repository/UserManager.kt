package com.allow.food4needy.repository

import com.allow.food4needy.domain.User

interface UserManager {

    val userLoggedIn: Boolean
    val userId: String
    val userRole: Int
    val userImageUrl: String
    val userName: String
    val userEmail: String
    val userPhone: String
    val userAlternatePhone: String
    val userAddress: String
    val currentUser: User

    fun skipSignIn(actionOnSuccess: () -> Unit, actionOnError: () -> Unit)
    fun getCurrentUser(actionOnSuccess: (User) -> Unit, actionOnError: () -> Unit)
    fun addUser(user: User, actionOnSuccess: () -> Unit, actionOnError: () -> Unit)
    fun getUserById(userId: String, actionOnSuccess: (User) -> Unit, actionOnError: () -> Unit)
}