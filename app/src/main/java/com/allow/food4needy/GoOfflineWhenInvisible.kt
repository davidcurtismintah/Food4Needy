package com.allow.food4needy

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber
import java.util.concurrent.TimeUnit


class GoOfflineWhenInvisible(private val fdb: FirebaseDatabase) : Application.ActivityLifecycleCallbacks {

    companion object {
        private val OFFLINE_DELAY = TimeUnit.SECONDS.toMillis(30)
    }

    private val handler = Handler()
    private var numActivitiesStarted: Int = 0

    private val goOffline = Runnable {
        Timber.i("Going offline now")
        fdb.goOffline()
    }

    private val goOnline = Runnable { fdb.goOnline() }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (numActivitiesStarted == 0) {
            Timber.i("Only activity started, going online")
            handler.removeCallbacks(goOffline)
            fdb.goOnline()
        }
        numActivitiesStarted++
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        numActivitiesStarted--
        if (numActivitiesStarted == 0) {
            Timber.i("Last activity stopped, going offline")
            handler.postDelayed(goOffline, OFFLINE_DELAY)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {}

}
