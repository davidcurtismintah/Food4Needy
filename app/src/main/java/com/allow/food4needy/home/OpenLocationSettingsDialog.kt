package com.allow.food4needy.home

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.allow.food4needy.R

class OpenLocationSettingsDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.gps_not_found_title)
        builder.setMessage(resources.getString(R.string.gps_not_found_message))
        builder.setPositiveButton(resources.getString(R.string.open_location_settings)) { _, _ ->
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            if (myIntent.resolveActivity(context!!.packageManager) != null) {
                startActivity(myIntent)
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { _, _ ->

        }
        return builder.create()
    }
}