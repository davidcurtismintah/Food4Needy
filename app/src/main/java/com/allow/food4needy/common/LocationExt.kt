package com.allow.food4needy.common

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.allow.food4needy.Food4NeedyApplication
import com.allow.food4needy.home.OpenLocationSettingsDialog

private const val TWO_MINUTES = 1000 * 60 * 2
const val TEN_METERS = 10f

/** Determines whether one Location reading is better than the current Location fix
 * @param location  The new Location that you want to evaluate
 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
 */
fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
    // A new location is always better than no location
    currentBestLocation ?: return true

    if (currentBestLocation.distanceTo(location) < TEN_METERS) return false
    // Check whether the new location fix is newer or older
    val timeDelta = location.time - currentBestLocation.time
    val isSignificantlyNewer = timeDelta > TWO_MINUTES
    val isSignificantlyOlder = timeDelta < -TWO_MINUTES
    val isNewer = timeDelta > 0

    // If it's been more than two minutes since the current location, use the new location
    // because the user has likely moved
    if (isSignificantlyNewer) {
        return true
        // If the new location is more than two minutes older, it must be worse
    } else if (isSignificantlyOlder) {
        return false
    }

    // Check whether the new location fix is more or less accurate
    val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
    val isLessAccurate = accuracyDelta > 0
    val isMoreAccurate = accuracyDelta < 0
    val isSignificantlyLessAccurate = accuracyDelta > 200

    // Check if the old and new location are from the same provider
    val isFromSameProvider = isSameProvider(location.provider,
            currentBestLocation.provider)

    // Determine location quality using a combination of timeliness and accuracy
    if (isMoreAccurate) {
        return true
    } else if (isNewer && !isLessAccurate) {
        return true
    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
        return true
    }
    return false
}

/** Checks whether two providers are the same  */
private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
    return if (provider1 == null) {
        provider2 == null
    } else provider1 == provider2
}

val getCountryIso: String
    get() {
        val telephonyManager = Food4NeedyApplication.instance.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.simCountryIso
    }



// ----------------------------

@Suppress("DEPRECATION")
fun AppCompatActivity.checkLocationServicesAvailability(): Boolean {
    val available: Boolean
    when {
        Build.VERSION.SDK_INT >= 28 -> {
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            available = lm.isLocationEnabled
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
            available = try {
                (Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE)) != Settings.Secure.LOCATION_MODE_OFF
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                false
            }
        }
        else -> {
            val locationProviders = Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
            available = !TextUtils.isEmpty(locationProviders)
        }
    }

    val dialogFragment = supportFragmentManager.findFragmentByTag("OpenLocationSettingsDialog") as? DialogFragment
    if (!available) {
        if (dialogFragment == null) {
            OpenLocationSettingsDialog().show(supportFragmentManager, "OpenLocationSettingsDialog")
        }
    } else {
        dialogFragment?.dismiss()
    }

    return available
}
