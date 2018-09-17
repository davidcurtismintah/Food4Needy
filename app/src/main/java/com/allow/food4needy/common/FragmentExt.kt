package com.allow.food4needy.common

import android.support.v4.app.Fragment
import android.util.DisplayMetrics

fun Fragment.convertPixelsToDp(px: Float): Float {
    val resources = getResources()
    val metrics = resources.getDisplayMetrics()
    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}
