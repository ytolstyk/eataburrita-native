package com.tolstykh.eatABurrita.location

import com.google.android.gms.maps.model.LatLng
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetLocationUseCase @Inject constructor(
    private val locationService: ILocationService
) {
    operator fun invoke(): Flow<LatLng?> = locationService.requestLocationUpdates()
}