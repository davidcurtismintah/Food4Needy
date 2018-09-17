package com.allow.food4needy.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.allow.food4needy.common.checkLocationServicesAvailability
import com.allow.food4needy.domain.NearbyData
import com.allow.food4needy.domain.Response
import com.allow.food4needy.domain.Status
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_list.*

abstract class BaseNearbyDataFragment<D : NearbyData, VH : RecyclerView.ViewHolder> : BaseDataFragment() {

    abstract var mAdapter: BaseNearbyDataRecyclerAdapter<D, VH>

    private val mLocationBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val latitude = intent.getDoubleExtra(UserTrackingService.EXTRA_LATITUDE, 0.0)
            val longitude = intent.getDoubleExtra(UserTrackingService.EXTRA_LONGITUDE, 0.0)

            val geoLocation = GeoLocation(latitude, longitude)

            onReceiveLocationUpdate(geoLocation)
        }
    }

    override fun setupList() {
        recyclerview.adapter = mAdapter
        showEmptyView(false)
    }

    override fun onResume() {
        super.onResume()

        val client = LocationServices.getFusedLocationProviderClient(context!!)
        val permission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client?.lastLocation?.addOnSuccessListener {
                it?.let {
                    val geoLocation = GeoLocation(it.latitude, it.longitude)
                    onReceiveLocationUpdate(geoLocation)
                }
            }
        }

        LocalBroadcastManager.getInstance(context!!).registerReceiver(
                mLocationBroadcastReceiver,
                IntentFilter(UserTrackingService.ACTION_LOCATION_BROADCAST)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(mLocationBroadcastReceiver)
    }

    protected fun handleNearbyDataResponse(response: Response<MutableList<D>, String>?) {
        response?.let { _response ->
            when (_response.status) {
                Status.LOADING -> {
                    showLoading(true)
                }

                Status.SUCCESS -> {
                    showLoading(false)
                    if (_response.data != null) {
                        if (_response.data.isNotEmpty()) {
                            showEmptyView(false)
                            mAdapter.refresh(_response.data)
                        } else {
                            showEmptyView(true)
                        }
                    } else {
                        showEmptyView(true)
                    }
                }

                Status.ERROR -> {
                    showLoading(false)
                }
            }
        }

    }

    abstract fun onReceiveLocationUpdate(geoLocation: GeoLocation)

}