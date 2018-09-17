package com.allow.food4needy.home

import android.support.annotation.MenuRes
import android.support.v4.app.Fragment
import com.allow.food4needy.R
import com.allow.food4needy.domain.UserRole
import com.allow.food4needy.home.mydonations.MyDonationsFragment
import com.allow.food4needy.home.nearbydonations.NearbyDonationsFragment
import com.allow.food4needy.home.nearbyvolunteers.NearbyVolunteersFragment

sealed class RoleAdapter(@MenuRes val navMenuResId: Int) {
    abstract fun getCount(): Int
    abstract fun getItem(position: Int): Fragment
}

class GuestRoleAdapter : RoleAdapter(R.menu.bottom_navigation_guest) {
    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> NearbyDonationsFragment.newInstance(UserRole.DONOR)
        1 -> NearbyDonationsFragment.newInstance(UserRole.RESTAURANT)
        else -> throw IllegalArgumentException("position: $position cannot be greater than ${getCount()}")
    }
}

class DonorRoleAdapter : RoleAdapter(R.menu.bottom_navigation_donors) {
    override fun getCount(): Int = 3

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> MyDonationsFragment.newInstance()
        1 -> NearbyDonationsFragment.newInstance(UserRole.DONOR)
        2 -> NearbyVolunteersFragment.newInstance()
        else -> throw IllegalArgumentException("position: $position cannot be greater than ${getCount()}")
    }
}

class RestaurantRoleAdapter : RoleAdapter(R.menu.bottom_navigation_restaurants) {
    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> MyDonationsFragment.newInstance()
        1 -> NearbyVolunteersFragment.newInstance()
        else -> throw IllegalArgumentException("position: $position cannot be greater than ${getCount()}")
    }
}

class VolunteerRoleAdapter : RoleAdapter(R.menu.bottom_navigation_volunteers) {
    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> NearbyDonationsFragment.newInstance(UserRole.DONOR)
        1 -> NearbyDonationsFragment.newInstance(UserRole.RESTAURANT)
        else -> throw IllegalArgumentException("position: $position cannot be greater than ${getCount()}")
    }
}