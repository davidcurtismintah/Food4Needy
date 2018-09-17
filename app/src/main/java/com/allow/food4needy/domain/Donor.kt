package com.allow.food4needy.domain

import java.util.*

class Donor(
        val id: String = UUID.randomUUID().toString(),
        val userRole: UserRole = UserRole.GUEST,
        val name: String = "",
        val address: String = "",
        val country: String ="",
        val phone: String = "",
        val dateOfBirth: String = "",
        val email: String = ""
)
