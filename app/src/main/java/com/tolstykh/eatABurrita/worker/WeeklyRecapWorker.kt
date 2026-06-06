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
import java.time.Instant
import java.time.temporal.ChronoUnit

@HiltWorker
class WeeklyRecapWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dao: BurritoDao,
    private val appPrefs: AppPreferencesRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!appPrefs.weeklyRecapEnabled.first()) return Result.success()

        val sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli()
        val count = dao.getEntriesSince(sevenDaysAgo).first().size

        BurritoNotificationManager.sendWeeklyRecap(applicationContext, count)
        return Result.success()
    }
}
