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
import dagger.hilt.android.HiltAndroidApp
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
        scheduleReminderWork()
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
}
