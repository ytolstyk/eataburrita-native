package com.tolstykh.eatABurrita.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.tolstykh.eatABurrita.data.AppPreferencesRepository
import com.tolstykh.eatABurrita.notification.BurritoNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var appPrefs: AppPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return

        val locationName = event.triggeringGeofences
            ?.firstOrNull()
            ?.requestId ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!appPrefs.geofenceEnabled.first()) return@launch

                val lastNotified = appPrefs.getGeofenceLastNotifiedTime(locationName)
                val sixHoursMs = TimeUnit.HOURS.toMillis(6)
                if (System.currentTimeMillis() - lastNotified < sixHoursMs) return@launch

                BurritoNotificationManager.sendGeofenceAlert(context, locationName)
                appPrefs.recordGeofenceNotified(locationName, System.currentTimeMillis())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
