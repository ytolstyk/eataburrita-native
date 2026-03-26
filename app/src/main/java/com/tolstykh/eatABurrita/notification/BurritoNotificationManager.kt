package com.tolstykh.eatABurrita.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.tolstykh.eatABurrita.MainActivity
import com.tolstykh.eatABurrita.R

object BurritoNotificationManager {

    private const val CHANNEL_ID = "burrito_reminder"
    private const val THREE_DAY_NOTIFICATION_ID = 1001
    private const val SEVEN_DAY_NOTIFICATION_ID = 1002

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Burrito Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminds you to log your burrito consumption"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun sendThreeDayReminder(context: Context) {
        sendNotification(
            context = context,
            notificationId = THREE_DAY_NOTIFICATION_ID,
            title = "Time for a burrito!",
            message = "It's totally ok to treat yourself to a burrito today.",
            openMap = false
        )
    }

    fun sendSevenDayReminder(context: Context) {
        sendNotification(
            context = context,
            notificationId = SEVEN_DAY_NOTIFICATION_ID,
            title = "Missing burritos?",
            message = "EatABurrita can find the nearest burrito place for you!",
            openMap = true
        )
    }

    private fun sendNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        openMap: Boolean
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (openMap) putExtra(MainActivity.EXTRA_OPEN_MAP, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_burrito)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }
}
