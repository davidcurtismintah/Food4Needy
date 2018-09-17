package com.allow.food4needy.common

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.allow.food4needy.R
import com.allow.food4needy.home.HomeActivity
import com.allow.food4needy.home.UserTrackingService
import java.util.*

//-------------notification extensions

val ONGOING_NOTIFICATION_ID = getRandomNumber()
val SMALL_ICON = R.drawable.ic_my_location
val STOP_ACTION_ICON = R.drawable.ic_radio_button_checked

/** PendingIntent to stop the service.  */
private fun getStopServicePI(context: Service): PendingIntent {
    lateinit var piStopService: PendingIntent
    run {
        val iStopService = MyIntentBuilder.getInstance(context, commandId = MyIntentBuilder.STOP).build()
        piStopService = PendingIntent.getService(context, getRandomNumber(), iStopService, 0)
    }
    return piStopService
}

/** Get pending intent to launch the activity.  */
private fun getLaunchActivityPI(context: Service): PendingIntent {
    lateinit var piLaunchMainActivity: PendingIntent
    run {
        val iLaunchMainActivity = Intent(context, HomeActivity::class.java)
        piLaunchMainActivity = PendingIntent.getActivity(context, getRandomNumber(), iLaunchMainActivity, 0)
    }
    return piLaunchMainActivity
}

//
// Pre O specific.
//

@Suppress("DEPRECATION")
@TargetApi(25)
object PreO {

    fun createNotification(context: Service) {
        // Create Pending Intents.
        val piLaunchMainActivity = getLaunchActivityPI(context)
        val piStopService = getStopServicePI(context)

        // Action to stop the service.
        val stopAction = NotificationCompat.Action.Builder(
                STOP_ACTION_ICON,
                getNotificationStopActionText(context),
                piStopService)
                .build()

        // Create a notification.
        val mNotification = NotificationCompat.Builder(context)
                .setContentTitle(getNotificationTitle(context))
                .setContentText(getNotificationContent(context))
                .setSmallIcon(SMALL_ICON)
                .setContentIntent(piLaunchMainActivity)
                .addAction(stopAction)
                .setStyle(NotificationCompat.BigTextStyle())
                .build()

        context.startForeground(ONGOING_NOTIFICATION_ID, mNotification)
    }
}

private fun getNotificationContent(context: Service): String {
    return context.getString(R.string.notification_text_content)
}

private fun getNotificationTitle(context: Service): String {
    return context.getString(R.string.notification_text_title)
}

//
// O Specific.
//

@Suppress("DEPRECATION")
@TargetApi(26)
object O {

    val CHANNEL_ID = getRandomNumber().toString()

    fun createNotification(context: Service) {
        val channelId = createChannel(context)
        val notification = buildNotification(context, channelId)
        context.startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun buildNotification(context: Service, channelId: String): Notification {
        // Create Pending Intents.
        val piLaunchMainActivity = getLaunchActivityPI(context)
        val piStopService = getStopServicePI(context)

        // Action to stop the service.
        val stopAction = Notification.Action.Builder(
                STOP_ACTION_ICON,
                getNotificationStopActionText(context),
                piStopService)
                .build()

        // Create a notification.
        return Notification.Builder(context, channelId)
                .setContentTitle(getNotificationTitle(context))
                .setContentText(getNotificationContent(context))
                .setSmallIcon(SMALL_ICON)
                .setContentIntent(piLaunchMainActivity)
                .setActions(stopAction)
                .setStyle(Notification.BigTextStyle())
                .build()
    }

    private fun createChannel(context: Service): String {
        // Create a channel.
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelName = "Location channel"
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel = NotificationChannel(CHANNEL_ID, channelName, importance)
        notificationManager.createNotificationChannel(notificationChannel)
        return CHANNEL_ID
    }
}

private fun getNotificationStopActionText(context: Service): String {
    return context.getString(R.string.notification_stop_action_text)
}



//-------------service extensions
fun getRandomNumber(): Int {
    return Random().nextInt(100000)
}

fun isPreAndroidO(): Boolean {
    return Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1
}



//-------------intent extensions
internal class MyIntentBuilder private constructor(val mContext: Context, val mMessage: String?, @Command val mCommandId: Int = INVALID) {
    companion object {
        val INVALID = -1
        val STOP = 0
        val START = 1

        private val KEY_MESSAGE = "msg"
        private val KEY_COMMAND = "cmd"

        //@IntDef(Command.INVALID, Command.STOP, Command.START)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Command

        fun getInstance(context: Context, message: String? = null, @Command commandId: Int = INVALID): MyIntentBuilder {
            return MyIntentBuilder(context, message, commandId)
        }

        fun containsCommand(intent: Intent): Boolean {
            return intent.extras!!.containsKey(KEY_COMMAND)
        }

        fun containsMessage(intent: Intent): Boolean {
            return intent.extras!!.containsKey(KEY_MESSAGE)
        }

        @Command
        fun getCommand(intent: Intent): Int {
            return intent.extras!!.getInt(KEY_COMMAND)
        }

        fun getMessage(intent: Intent): String? {
            return intent.extras!!.getString(KEY_MESSAGE)
        }
    }

    fun build(): Intent {
        val intent = Intent(mContext, UserTrackingService::class.java)
        if (mCommandId != INVALID) {
            intent.putExtra(KEY_COMMAND, mCommandId)
        }
        if (mMessage != null) {
            intent.putExtra(KEY_MESSAGE, mMessage)
        }
        return intent
    }
}
