package com.tolstykh.eatABurrita

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tolstykh.eatABurrita.notification.BurritoNotificationManager
import com.tolstykh.eatABurrita.worker.BurritoReminderWorker
import com.tolstykh.eatABurrita.worker.WeeklyRecapWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

val Context.appPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

@HiltAndroidApp
class EatABurrita : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        BurritoNotificationManager.createNotificationChannel(this)
        BurritoNotificationManager.createGeofenceNotificationChannel(this)
        BurritoNotificationManager.createStreakNotificationChannel(this)
        scheduleReminderWork()
        scheduleWeeklyRecapWork()
    }

    private fun scheduleReminderWork() {
        val workRequest = PeriodicWorkRequestBuilder<BurritoReminderWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "burrito_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun scheduleWeeklyRecapWork() {
        val now = Calendar.getInstance()
        val daysUntilMonday = (Calendar.MONDAY - now.get(Calendar.DAY_OF_WEEK) + 7) % 7
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val hoursUntilMondayMorning = daysUntilMonday * 24L + (9 - currentHour)
        val initialDelayHours = hoursUntilMondayMorning.coerceAtLeast(1)

        val workRequest = PeriodicWorkRequestBuilder<WeeklyRecapWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(initialDelayHours, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weekly_recap",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
