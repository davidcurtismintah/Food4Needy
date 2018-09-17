package com.allow.food4needy.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.support.annotation.IdRes
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.allow.food4needy.GlideApp
import com.allow.food4needy.R
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Returns a lazy delegate which provides a view found by its id.
 */
fun <T : View> RecyclerView.ViewHolder.bindView(layoutId: Int) = lazy { itemView.findViewById<T>(layoutId)!! }

/**
 * Get intent from specified class
 * */
inline fun <reified T : Activity> Context.getIntent() = Intent(this, T::class.java)

/**
 * Get an extra with a default value from an intent
 * */
fun <T : Parcelable> Activity.extra(key: String, default: T? = null): Lazy<T> =
        lazy {
            intent?.extras?.getParcelable<T>(key) ?: default
            ?: throw Error("No value $key in extras")
        }

/**
 * Make a toast
 * */
fun Context.toast(message: String, length: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, length).show()
}

/**
 * delegates a property's value to a [SwipeRefreshLayout]'s [isRefreshing][SwipeRefreshLayout.isRefreshing] value
 * */
fun Activity.bindToSwipeRefresh(@IdRes resourceId: Int) = object : ReadWriteProperty<Any?, Boolean> {

    val swipeRefreshView by lazy { findViewById<SwipeRefreshLayout>(resourceId) }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return swipeRefreshView.isRefreshing
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        swipeRefreshView.isRefreshing = value
    }

}

/**
 * Loads an image into an [imageview][ImageView] from a url
 * */
fun ImageView.loadImage(imageUrl: String, centreCropped: Boolean = false, placeholder: Int = R.color.bgGrey, error: Int = placeholder) {
    GlideApp.with(context)
            .load(resources.getIdentifier(imageUrl, "drawable", context.packageName))
            .apply { if (centreCropped) centerCrop() }
            .placeholder(placeholder)
            .error(error)
            .into(this)
}
