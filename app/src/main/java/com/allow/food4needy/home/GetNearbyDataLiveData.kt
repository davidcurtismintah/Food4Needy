package com.allow.food4needy.home

import android.app.Application
import android.arch.lifecycle.LiveData
import android.location.Location
import com.allow.food4needy.common.mGson
import com.allow.food4needy.domain.Donation
import com.allow.food4needy.domain.NearbyData
import com.allow.food4needy.domain.Response
import com.allow.food4needy.domain.User
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import kotlin.Comparator


abstract class GetNearbyDataLiveData<D : NearbyData, E>(
        private val application: Application,
        keyRef: DatabaseReference,
        private val dataRef: DatabaseReference
) : LiveData<Response<MutableList<D>, E>>() {

    companion object {

        const val TWENTY_KILOMETERS = 20.0

        class GetNearbyDonationsLiveData(application: Application,
                                         keyRef: DatabaseReference,
                                         dataRef: DatabaseReference
        ) : GetNearbyDataLiveData<Donation, String>(application, keyRef, dataRef) {
            override val getDataClass: Class<Donation> = Donation::class.java

            override fun createErrorMessage(error: DatabaseError): String = error.message
        }

        class GetNearbyVolunteersLiveData(application: Application,
                                          keyRef: DatabaseReference,
                                          dataRef: DatabaseReference
        ) : GetNearbyDataLiveData<User, String>(application, keyRef, dataRef) {
            override val getDataClass: Class<User> = User::class.java
            override fun createErrorMessage(error: DatabaseError): String = error.message
        }

        fun donations(application: Application,
                      keyRef: DatabaseReference,
                      dataRef: DatabaseReference) = GetNearbyDonationsLiveData(application, keyRef, dataRef)

        fun volunteers(application: Application,
                       keyRef: DatabaseReference,
                       dataRef: DatabaseReference) = GetNearbyVolunteersLiveData(application, keyRef, dataRef)
    }

//    private var listenerRemovePending = false
//    private val handler = Handler()
//    private var removeListener = Runnable {
//        removeListeners()
//        listenerRemovePending = false
//    }

    private var geoFire = GeoFire(keyRef)
    private var mGeoQuery: GeoQuery? = null

    private val data by lazy { mutableListOf<D>() }
    private lateinit var dataValueListener: ValueEventListener
    private var fetchedDataKeys = false
    private val dataKeysWithListeners by lazy { mutableSetOf<String>() }

    private var initialListSize = 0
    private var iterationCount = 0
    private var me = Location("me")
    private val dataKeysToLocations by lazy { mutableMapOf<String, Location>() }

    init {
        setupDataListener()
    }

//    override fun onActive() {
//        super.onActive()
//        if (listenerRemovePending) {
//            handler.removeCallbacks(removeListener)
//        } else {
//            fetchDonations()
//        }
//        listenerRemovePending = false
//    }
//
//    override fun onInactive() {
//        super.onInactive()
//        handler.postDelayed(removeListener, 2000)
//        listenerRemovePending = true
//    }

    private fun setupDataListener() {
        dataValueListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!fetchedDataKeys) {
                    iterationCount++
                }

                val donationKey = dataSnapshot.key
                if (donationKey != null) {
                    val jsonString: String = mGson.toJson(dataSnapshot.value)
//                    val mDataType: Type = object : TypeToken<D>() {}.type
                    val d: D? = mGson.fromJson(jsonString, getDataClass)
                    if (d != null) {
                        d.id = donationKey
                        val location = dataKeysToLocations[donationKey]
                        if (location != null) {
                            d.latitude = location.latitude
                            d.longitude = location.longitude
                            val position = getDonationPosition(donationKey)
                            if (position != -1) {
                                data.removeAt(position)
                                data.add(position, d)
                            } else {
                                data.add(d)
                            }
                            sortByDistanceFromMe()
                        }
                    } else {
                        val position = getDonationPosition(donationKey)
                        if (position != -1) {
                            data.removeAt(position)
                        }
                    }
                }

                if (!fetchedDataKeys && iterationCount == initialListSize) {
                    fetchedDataKeys = true
                    value = Response.success(data)
                } else if (fetchedDataKeys) {
                    value = Response.success(data)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.e(databaseError.toException(), "onCancelled: ")
                value = Response.error(createErrorMessage(databaseError))
            }
        }
    }

    private fun sortByDistanceFromMe() {
        data.sortWith(Comparator { u1, u2 ->
            val first = Location("")
            first.latitude = u1.latitude
            first.longitude = u1.longitude

            val second = Location("")
            second.latitude = u2.latitude
            second.longitude = u2.longitude

            when {
                me.distanceTo(first) > me.distanceTo(second) -> return@Comparator 1
                me.distanceTo(first) < me.distanceTo(second) -> return@Comparator -1
                else -> return@Comparator 0
            }
        })

        for (donation in data) {
            val location = Location("")
            location.latitude = donation.latitude
            location.longitude = donation.longitude

            Timber.d("newDonation: distance ${me.distanceTo(location)}")
        }
    }

    fun fetchDonations(geoLocation: GeoLocation) {

        me.latitude = geoLocation.latitude
        me.longitude = geoLocation.longitude

        if (mGeoQuery == null) {
            mGeoQuery = geoFire.queryAtLocation(geoLocation, TWENTY_KILOMETERS)
            setUpKeyListener()
        } else {
            mGeoQuery?.center = geoLocation
        }
    }

    private fun setUpKeyListener() {

//        if (!this::mGeoQuery.isInitialized) return

        mGeoQuery?.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                Timber.d("onKeyEntered: ")

                value = Response.loading()

                val to = Location("to")
                to.latitude = location.latitude
                to.longitude = location.longitude
                dataKeysToLocations[key] = to
                if (fetchedDataKeys) {
                    addDonationListener(key)
                }

            }

            override fun onKeyExited(key: String) {
                Timber.d("onKeyExited: ")
                if (dataKeysWithListeners.contains(key)) {
                    val position = getDonationPosition(key)
                    if (position != -1) {
                        data.removeAt(position)
                    }

                    dataKeysWithListeners.remove(key)
                    removeDataListener(key)

                    value = Response.success(data)
                }
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                Timber.d("onKeyMoved: ")
            }

            override fun onGeoQueryReady() {
                Timber.d("onGeoQueryReady: $fetchedDataKeys at $this")

                if (!fetchedDataKeys) {
                    initialListSize = dataKeysToLocations.size
                    if (initialListSize == 0) {
                        fetchedDataKeys = true
                        value = Response.success(data)
                    }
                    iterationCount = 0

                    dataKeysToLocations.keys.forEach(this::addDonationListener)
                }
            }

            private fun addDonationListener(donationId: String) {
                dataRef.child(donationId).addValueEventListener(dataValueListener)

                dataKeysWithListeners.add(donationId)
            }

            override fun onGeoQueryError(error: DatabaseError) {
                Timber.e(error.toException(), "onGeoQueryError: ")

                value = Response.error(createErrorMessage(error))
            }
        })
    }

    private fun getDonationPosition(id: String) = data.indices.firstOrNull { data[it].id == id }
            ?: -1

    abstract val getDataClass: Class<D>
    abstract fun createErrorMessage(error: DatabaseError): E

    private fun removeListeners() {
        mGeoQuery?.removeAllListeners()

        for (donationId in dataKeysWithListeners) {
            removeDataListener(donationId)
        }
        dataKeysWithListeners.clear()
        dataKeysToLocations.clear()
    }

    private fun removeDataListener(donationId: String) {
        dataRef.child(donationId).removeEventListener(dataValueListener)
    }

    override fun onActive() {
        super.onActive()
        setUpKeyListener()
    }

    override fun onInactive() {
        super.onInactive()
        removeListeners()
    }
}
