package com.allow.food4needy

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

object CrashReportingTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.INFO

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        Crashlytics.setUserIdentifier(FirebaseAuth.getInstance().uid);

        Crashlytics.log(priority,  tag,  message)

        if (t != null) {
            Crashlytics.logException(t)
        }
    }

}