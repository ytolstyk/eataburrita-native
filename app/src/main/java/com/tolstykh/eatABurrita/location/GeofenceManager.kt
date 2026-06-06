package com.tolstykh.eatABurrita.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.tolstykh.eatABurrita.data.BurritoDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofencingClient: GeofencingClient,
    private val dao: BurritoDao,
) {
    companion object {
        private const val GEOFENCE_RADIUS_METERS = 300f
        private const val LOITERING_DELAY_MS = 60_000
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun registerTopLocationGeofences() {
        val locations = dao.getTopLocationsWithCoords()
        if (locations.isEmpty()) return

        geofencingClient.removeGeofences(geofencePendingIntent).await()

        val geofences = locations.map { location ->
            Geofence.Builder()
                .setRequestId(location.locationName)
                .setCircularRegion(location.locationLat, location.locationLong, GEOFENCE_RADIUS_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setLoiteringDelay(LOITERING_DELAY_MS)
                .build()
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(0) // don't fire immediately if already inside
            .addGeofences(geofences)
            .build()

        geofencingClient.addGeofences(request, geofencePendingIntent).await()
    }

    suspend fun removeAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent).await()
    }
}
