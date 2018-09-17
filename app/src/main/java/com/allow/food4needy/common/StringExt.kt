package com.allow.food4needy.common

fun String.toKg() = "${trim()} kg"

fun String.toHr() = this.trim().let { "Expires in $it ${if (it.toIntOrNull() == 1) "hr" else "hrs"}" }

fun String.toProgress(created: Long) = (((System.currentTimeMillis() - created) /
        (1000.0 * 60.0 * 60.0 * (toIntOrNull() ?: 0))) * 100.0).toInt()

fun String.toExpired(progress: Int) = progress >= 100

fun String.toExpiryText(progress: Int, str: String) = if (toExpired(progress)) str else toHr()

