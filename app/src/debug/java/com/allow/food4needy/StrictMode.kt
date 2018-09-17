package com.allow.food4needy

import android.os.StrictMode

val THREAD_POLICY: StrictMode.ThreadPolicy =
        StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyFlashScreen()
                .build()

val VM_POLICY: StrictMode.VmPolicy =
        StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
