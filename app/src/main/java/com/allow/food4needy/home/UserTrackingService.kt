package com.allow.food4needy.home

import android.Manifest
import android.annotation.TargetApi
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import com.allow.food4needy.common.*
import com.allow.food4needy.common.MyIntentBuilder.Companion.containsCommand
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber


class UserTrackingService : Service() {

    companion object {
        val ACTION_LOCATION_BROADCAST = UserTrackingService::class.java.name + "LocationBroadcast"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private var mServiceIsStarted = false
    private lateinit var mLocationCallback: LocationCallback
    private val mRequest = LocationRequest().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = TEN_METERS
    }
    private var mCurrentBestLocation: Location? = null

    private val mFusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private val mDatabase by lazy { FirebaseDatabase.getInstance().reference}

    override fun onCreate() {
        super.onCreate()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    if (isBetterLocation(it, mCurrentBestLocation)) {
                        mCurrentBestLocation = it

                        mDatabase.child("users").child("all").child("data").child(FirebaseAuth.getInstance().currentUser!!.uid).child("role").addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val roleValue = dataSnapshot.value as? Long

                                        fun sendGeolocationToUi(geoLocation: GeoLocation) {
                                            Timber.d("Sending geoLocation to activity")

                                            val intent = Intent(ACTION_LOCATION_BROADCAST)
                                            intent.putExtra(EXTRA_LATITUDE, geoLocation.latitude)
                                            intent.putExtra(EXTRA_LONGITUDE, geoLocation.longitude)
                                            LocalBroadcastManager.getInstance(this@UserTrackingService).sendBroadcast(intent)
                                        }
//
                                        if (roleValue == null) {
                                            Timber.w("User role is unexpectedly null")
                                        } else {
                                            Timber.d("User role obtained $roleValue")
                                            val mGeoFire = GeoFire(mDatabase.child("user-location").child(getCountryIso).child("role").child("$roleValue"))
                                            val geoLocation = GeoLocation(it.latitude, it.longitude)
                                            mGeoFire.setLocation(FirebaseAuth.getInstance().currentUser!!.uid, geoLocation, object : GeoFire.CompletionListener {
                                                override fun onComplete(key: String?, error: DatabaseError?) {
                                                    Timber.d( "GeoLocation updated")
                                                }

                                            })

                                            sendGeolocationToUi(geoLocation)

                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        Timber.e(databaseError.toException(), "getUserRole:onCancelled")
                                    }
                                })
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val containsCommand = MyIntentBuilder.containsCommand(intent)
        Timber.d("onStartCommand: Service in [%s] state. commandId: [%d]. startId: [%d]",
                if (mServiceIsStarted) "STARTED" else "NOT STARTED",
                if (containsCommand) MyIntentBuilder.getCommand(intent) else -1,
                startId)
        mServiceIsStarted = true
        routeIntentToCommand(intent)
        return Service.START_NOT_STICKY
    }

    private fun routeIntentToCommand(intent: Intent?) {
        if (intent != null) {

            if (containsCommand(intent)) {
                processCommand(MyIntentBuilder.getCommand(intent))
            }

            if (MyIntentBuilder.containsMessage(intent)) {
                processMessage(MyIntentBuilder.getMessage(intent))
            }
        }
    }

    private fun processMessage(message: String?) {
        try {
            Timber.d(String.format("doMessage: message from client: '%s'", message))
        } catch (e: Exception) {
            Timber.e(e, "processMessage: exception")
        }

    }

    private fun processCommand(command: Int) {
        try {
            when (command) {
                MyIntentBuilder.START -> commandStart()
                MyIntentBuilder.STOP -> commandStop()
            }
        } catch (e: Exception) {
            Timber.e(e, "processCommand: exception")
        }

    }

    /**
     * This method can be called directly, or by firing an explicit Intent with [ ][MyIntentBuilder.STOP].
     */
    private fun commandStop() {
        stopLocationUpdates()
        stopForeground(true)
        stopSelf()
        mServiceIsStarted = false
    }

    /**
     * This can be called via an explicit intent to start this service, which calls [ ][.onStartCommand] or it can be called directly, which is what happens in
     * [.onClick] by this bound service.
     *
     *
     *
     *
     *
     * This is why the service needs to [.moveToStartedState] if it's not already in a
     * started state. More details can be found in the method documentation itself.
     */
    private fun commandStart() {

        if (!mServiceIsStarted) {
            moveToStartedState()
            return
        }

        if (FirebaseAuth.getInstance().currentUser != null) {
            startLocationUpdates()
        } else {
            Timber.d("Cannot start location tracking-----User not authenticated")
        }

        if (isPreAndroidO()) {
            PreO.createNotification(this)
        } else {
            O.createNotification(this)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun moveToStartedState() {

        val intent = MyIntentBuilder.getInstance(this, commandId = MyIntentBuilder.START).build()
        if (isPreAndroidO()) {
            Timber.d("moveToStartedState: Running on Android N or lower - startService(intent)")
            startService(intent)
        } else {
            Timber.d("moveToStartedState: Running on Android O - startForegroundService(intent)")
            startForegroundService(intent)
        }
    }

    private fun startLocationUpdates() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mRequest, mLocationCallback, null)
        }
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }
}