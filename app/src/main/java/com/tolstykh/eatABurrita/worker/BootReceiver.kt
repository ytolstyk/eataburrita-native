package com.tolstykh.eatABurrita.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tolstykh.eatABurrita.data.AppPreferencesRepository
import com.tolstykh.eatABurrita.location.GeofenceManager
import com.tolstykh.eatABurrita.location.hasBackgroundLocationPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var appPrefs: AppPreferencesRepository
    @Inject lateinit var geofenceManager: GeofenceManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (appPrefs.geofenceEnabled.first() && context.hasBackgroundLocationPermission()) {
                    geofenceManager.registerTopLocationGeofences()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
