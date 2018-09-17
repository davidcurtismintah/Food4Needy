package com.allow.food4needy.common

import android.os.Build
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics

fun AppCompatActivity.addFragment(@IdRes containerId: Int, fragment: Fragment) {
    if (!fragment.isStateSaved)
        supportFragmentManager
                .beginTransaction()
                .add(containerId, fragment)
                .commit()
}

fun AppCompatActivity.addFragmentToStack(@IdRes containerId: Int, fragment: Fragment) {
    if (!fragment.isStateSaved)
        supportFragmentManager
                .beginTransaction()
                .add(containerId, fragment)
                .addToBackStack(null)
                .commit()
}

fun AppCompatActivity.replaceFragment(@IdRes containerId: Int, fragment: Fragment) {
    if (!fragment.isStateSaved)
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commit()
}

fun AppCompatActivity.removeFragment(fragment: Fragment) {
    if (!fragment.isStateSaved)
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                .remove(fragment)
                .commit()
}

val AppCompatActivity.width
    get() = window.decorView.width

val AppCompatActivity.height
    get() = window.decorView.height

fun AppCompatActivity.convertPixelsToDp(px: Float): Float {
    val resources = getResources()
    val metrics = resources.getDisplayMetrics()
    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

val AppCompatActivity.isPreAndroidO
    get() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1
