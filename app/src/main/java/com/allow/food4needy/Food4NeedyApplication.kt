package com.allow.food4needy

import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import android.support.multidex.MultiDex
import android.util.Log
import android.util.Log.INFO
import com.allow.food4needy.di.DaggerAppComponent
import com.allow.food4needy.repository.FirebaseUserManager
import com.google.firebase.database.FirebaseDatabase
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import timber.log.Timber
import timber.log.Timber.DebugTree
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import android.widget.Toast
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task


class Food4NeedyApplication : DaggerApplication() {

    companion object {

        private const val USER_PREFS = "user_prefs"

//        @get:Synchronized
        lateinit var instance: Food4NeedyApplication
            private set

        lateinit var refWatcher: RefWatcher
            private set

        lateinit var prefs: SharedPreferences
            private set

        val userManager by lazy { FirebaseUserManager }

        val fdb by lazy { FirebaseDatabase.getInstance() }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        refWatcher = LeakCanary.install(this)

        prefs = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)

        Timber.plant(LOGGING_INSTANCE)

        StrictMode.setThreadPolicy(THREAD_POLICY)
        StrictMode.setVmPolicy(VM_POLICY)

        initFirebase()
    }

    private fun initFirebase() {
        fdb.setPersistenceEnabled(true)
        registerActivityLifecycleCallbacks(GoOfflineWhenInvisible(fdb))

//        fdb.setPersistenceEnabled(false)
//        fdb.purgeOutstandingWrites()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.create()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}
