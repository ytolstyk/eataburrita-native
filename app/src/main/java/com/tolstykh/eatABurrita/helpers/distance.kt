package com.tolstykh.eatABurrita.helpers

import android.location.Location
import com.google.android.gms.maps.model.LatLng

const val METERS_TO_MILES_COEFFICIENT = 0.000621371f

fun distanceBetweenInMiles(latLng1: LatLng?, latLng2: LatLng?): String {
    if (latLng1 == null || latLng2 == null) return ""

    val results = FloatArray(1)
    Location.distanceBetween(
        latLng1.latitude, latLng1.longitude,
        latLng2.latitude, latLng2.longitude,
        results
    )

    return  "%.2f".format(results[0] * METERS_TO_MILES_COEFFICIENT)
}