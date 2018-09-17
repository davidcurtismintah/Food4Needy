package com.allow.food4needy.profile

import android.content.Context
import android.support.v4.app.Fragment
import com.allow.food4needy.R
import com.allow.food4needy.common.PlaceHolderFragment
import com.allow.food4needy.profile.donationssummary.DonationsSummaryFragment
import com.allow.food4needy.profile.info.ProfileInfoFragment

sealed class RoleAdapter {
    abstract fun getItem(position: Int): Fragment
    abstract fun getCount(): Int
    abstract fun getPageTitle(position: Int): String
}

class GuestRoleAdapter : RoleAdapter() {
    override fun getItem(position: Int): Fragment = throw IllegalArgumentException("should not be called")

    override fun getCount(): Int = 0

    override fun getPageTitle(position: Int): String = ""
}

class DonorRoleAdapter(val ctx: Context) : RoleAdapter() {
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> ProfileInfoFragment.newInstance()
        1 -> DonationsSummaryFragment.newInstance()
        2 -> PlaceHolderFragment.newInstance("Coming soon")
        else -> throw IllegalArgumentException("position: $position cannot be greater than ${getCount()}")
    }

    override fun getCount(): Int  = 3

    override fun getPageTitle(position: Int): String = when (position) {
        0 -> ctx.getString(R.string.heading_info)
        1 -> ctx.getString(R.string.heading_donations)
        2 -> ctx.getString(R.string.heading_rewards)
        else -> throw IllegalArgumentException("position: $position cannot be greater than ${getCount()}")
    }
}

class RestaurantRoleAdapter(val ctx: Context) : RoleAdapter() {
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> ProfileInfoFragment.newInstance()
        1 -> PlaceHolderFragment.newInstance("Coming soon")
        else -> throw IllegalArgumentException("position: $position cannot be greater than ${getCount()}")
    }

    override fun getCount(): Int  = 2

    override fun getPageTitle(position: Int): String = when (position) {
        0 -> ctx.getString(R.string.heading_info)
        1 -> ctx.getString(R.string.heading_rewards)
        else -> throw IllegalArgumentException("position: $position cannot be greater than ${getCount()}")
    }
}

class VolunteerRoleAdapter(val ctx: Context) : RoleAdapter() {
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> ProfileInfoFragment.newInstance()
        1 -> PlaceHolderFragment.newInstance("Coming soon")
        else -> throw IllegalArgumentException("position: $position cannot be greater than ${getCount()}")
    }

    override fun getCount(): Int  = 2

    override fun getPageTitle(position: Int): String = when (position) {
        0 -> ctx.getString(R.string.heading_info)
        1 -> ctx.getString(R.string.heading_rewards)
        else -> throw IllegalArgumentException("position: $position cannot be greater than ${getCount()}")
    }
}

