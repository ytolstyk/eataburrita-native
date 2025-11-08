package com.tolstykh.eatABurrita.ui.map

import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(onBackPressed: () -> Unit = {}) {
    Surface {
        Column {
            Button(onClick = onBackPressed) {
                Text("Back")
            }
            GoogleMapView()
        }
    }
}

@Composable
fun GoogleMapView() {
    // This manages the current camera position, which includes: LatLng (center of the map),
    // zoom level, bearing (rotation) & tilt (3D perspective).
//    val cameraPositionState = rememberCameraPositionState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
    }
    val locationState = remember { mutableStateOf<Location?>(null) }
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)

//    LaunchedEffect(key1 = currentLocation) {
//        currentLocation?.let {
//            cameraPositionState.animate(
//                update = CameraUpdateFactory.newLatLngZoom(it, 15f), // Adjust zoom as needed
//                durationMs = 1000 // Animation duration
//            )
//        }
//    }

    GoogleMap(
        // Modifier defines how the map should be laid out in the UI.
        // fillMaxSize() ensures it takes up all available screen space.
        modifier = Modifier.fillMaxSize(),

        // Holds the current camera position state, including location (LatLng), zoom, tilt, and bearing.
        // You can also control or animate the camera from here.
        cameraPositionState = cameraPositionState,

        // MapProperties define the functional behavior of the map.
        // This includes things like enabling the user's current location,
        // choosing the map type (normal, satellite, terrain), traffic overlays, etc.
        properties = MapProperties(
            // Optional extras:
            // mapType = MapType.NORMAL,
            // isTrafficEnabled = true,
            // isBuildingsEnabled = true,
            // minZoomPreference = 10f,
            // maxZoomPreference = 20f
        ),

        // MapUiSettings control the user interface and gestures available on the map.
        // This includes zoom buttons, gestures (scrolling, rotating, tilting),
        // compass visibility, and whether the "My Location" button appears.
        uiSettings = MapUiSettings(
            // Shows the "My Location" button if permission is granted.
            // Tapping it animates the camera to the user's current location.
            // Optional extras:
            // compassEnabled = true,
            // zoomControlsEnabled = true,
            // scrollGesturesEnabled = true,
            // rotateGesturesEnabled = true,
            // tiltGesturesEnabled = false
        )
    )
}