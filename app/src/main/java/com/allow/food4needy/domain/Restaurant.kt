package com.allow.food4needy.domain

import java.util.*

class Restaurant(
        val id: String = UUID.randomUUID().toString(),
        val imageUrl: String = "",
        val name: String = "",
        val address: String = ""
)
