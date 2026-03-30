package com.tolstykh.eatABurrita.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tolstykh.eatABurrita.data.AppPreferencesRepository
import com.tolstykh.eatABurrita.data.BurritoDao
import com.tolstykh.eatABurrita.notification.BurritoNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class BurritoReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val burritoDao: BurritoDao,
    private val appPrefs: AppPreferencesRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!appPrefs.notificationsEnabled.first()) return Result.success()

        val latestTimestamp = burritoDao.getLatestTimestampOnce()
            ?: return Result.success() // No entries yet — new user, skip

        val daysSince = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - latestTimestamp)
        when {
            daysSince >= 7 -> {
                if (!appPrefs.sevenDayNotified.first()) {
                    BurritoNotificationManager.sendSevenDayReminder(applicationContext)
                    appPrefs.setSevenDayNotified(true)
                    appPrefs.setThreeDayNotified(true)
                }
            }
            daysSince >= 3 -> {
                if (!appPrefs.threeDayNotified.first()) {
                    BurritoNotificationManager.sendThreeDayReminder(applicationContext)
                    appPrefs.setThreeDayNotified(true)
                }
            }
        }

        return Result.success()
    }
}
