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

    private const val REMINDER_CHANNEL_ID = "burrito_reminder"
    private const val GEOFENCE_CHANNEL_ID = "burrito_geofence"
    private const val STREAK_CHANNEL_ID = "burrito_streak"

    private const val THREE_DAY_NOTIFICATION_ID = 1001
    private const val SEVEN_DAY_NOTIFICATION_ID = 1002
    private const val GEOFENCE_NOTIFICATION_ID = 2001
    private const val STREAK_NOTIFICATION_BASE_ID = 3000
    private const val WEEKLY_RECAP_NOTIFICATION_ID = 4001

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            "Burrito Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminds you to log your burrito consumption"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun createGeofenceNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            GEOFENCE_CHANNEL_ID,
            "Nearby Burrito Spots",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when you're near a favorite burrito spot"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun createStreakNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            STREAK_CHANNEL_ID,
            "Streak Milestones",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Celebrates your burrito eating streaks"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun sendThreeDayReminder(context: Context) {
        sendNotification(
            context = context,
            channelId = REMINDER_CHANNEL_ID,
            notificationId = THREE_DAY_NOTIFICATION_ID,
            title = "Time for a burrito!",
            message = "It's totally ok to treat yourself to a burrito today.",
            openMap = false
        )
    }

    fun sendSevenDayReminder(context: Context) {
        sendNotification(
            context = context,
            channelId = REMINDER_CHANNEL_ID,
            notificationId = SEVEN_DAY_NOTIFICATION_ID,
            title = "Missing burritos?",
            message = "EatABurrita can find the nearest burrito place for you!",
            openMap = true
        )
    }

    fun sendGeofenceAlert(context: Context, locationName: String) {
        sendNotification(
            context = context,
            channelId = GEOFENCE_CHANNEL_ID,
            notificationId = GEOFENCE_NOTIFICATION_ID,
            title = "You're near $locationName!",
            message = "Time for a burrito? 🌯",
            openMap = false
        )
    }

    fun sendStreakMilestoneNotification(context: Context, streakDays: Int) {
        val message = when (streakDays) {
            7 -> "You're a burrito legend! 🌯"
            14 -> "Two weeks strong! Unstoppable! 🔥"
            30 -> "A full month! You're the GOAT! 🏆"
            50 -> "50 days?! You ARE a burrito! 🤯"
            else -> "Keep the streak alive! 🌯"
        }
        sendNotification(
            context = context,
            channelId = STREAK_CHANNEL_ID,
            notificationId = STREAK_NOTIFICATION_BASE_ID + streakDays,
            title = "$streakDays-day streak!",
            message = message,
            openMap = false
        )
    }

    fun sendWeeklyRecap(context: Context, burritoCount: Int) {
        val message = when {
            burritoCount == 0 -> "No burritos last week? Time to fix that! 🌯"
            burritoCount == 1 -> "1 burrito last week. A solid start!"
            burritoCount <= 3 -> "$burritoCount burritos last week. Keep the streak going! 🔥"
            else -> "$burritoCount burritos last week! You're a machine! 🤩"
        }
        sendNotification(
            context = context,
            channelId = REMINDER_CHANNEL_ID,
            notificationId = WEEKLY_RECAP_NOTIFICATION_ID,
            title = "Your weekly burrito recap",
            message = message,
            openMap = false
        )
    }

    private fun sendNotification(
        context: Context,
        channelId: String,
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
        val notification = NotificationCompat.Builder(context, channelId)
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
