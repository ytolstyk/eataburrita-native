package com.tolstykh.eatABurrita.ui.map

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindowComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.tolstykh.eatABurrita.BuildConfig
import com.tolstykh.eatABurrita.R
import com.tolstykh.eatABurrita.helpers.statusBarHeight
import com.tolstykh.eatABurrita.location.hasLocationPermission
import com.tolstykh.eatABurrita.readablePlaceAddress
import com.tolstykh.eatABurrita.ui.theme.LocalExColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapScreenViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {}
) {
    MapsInitializer.initialize(LocalContext.current)
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    Surface {
        LaunchedEffect(!context.hasLocationPermission()) {
            permissionState.launchMultiplePermissionRequest()
        }

        when {
            permissionState.allPermissionsGranted -> {
                LaunchedEffect(Unit) {
                    (viewModel::handle)(PermissionEvent.Granted)
                }
            }

            permissionState.shouldShowRationale -> {
                RationaleAlert(onDismiss = { }) {
                    permissionState.launchMultiplePermissionRequest()
                }
            }

            !permissionState.allPermissionsGranted && !permissionState.shouldShowRationale -> {
                LaunchedEffect(Unit) {
                    (viewModel::handle)(PermissionEvent.Revoked)
                }
            }
        }

        with(viewState) {
            when (this) {
                ViewState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                ViewState.RevokedPermissions -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("We need permissions to use this app")
                        Button(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            },
                            enabled = !context.hasLocationPermission()
                        ) {
                            if (context.hasLocationPermission()) CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White
                            )
                            else Text("Settings")
                        }
                    }
                }

                is ViewState.Success -> {
                    val currentLoc =
                        LatLng(
                            location?.latitude ?: 0.0,
                            location?.longitude ?: 0.0
                        )
                    val cameraState = rememberCameraPositionState()

                    LaunchedEffect(key1 = currentLoc) {
                        var counter = 0
                        while (currentLoc.latitude == 0.0 && currentLoc.longitude == 0.0 && counter < 25) {
                            delay(200L)
                            counter++
                        }

                        cameraState.centerOnLocation(currentLoc)
                    }

                    FullMapView(
                        currentPosition = LatLng(
                            currentLoc.latitude,
                            currentLoc.longitude
                        ),
                        cameraState = cameraState,
                        onBackPressed = onBackPressed
                    )
                }
            }
        }
    }
}

@Composable
fun FullMapView(
    currentPosition: LatLng,
    cameraState: CameraPositionState,
    onBackPressed: () -> Unit = {},
) {
    val marker = LatLng(currentPosition.latitude, currentPosition.longitude)
    val markerState = remember {
        MarkerState(position = marker)
    }
    val places = remember { mutableStateListOf<Place>() }
    val context = LocalContext.current
    val placesClient: PlacesClient = remember {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context, BuildConfig.MAPS_API_KEY)
        }

        Places.createClient(context)
    }

    LaunchedEffect(key1 = currentPosition) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.LOCATION,
            Place.Field.ADDRESS_COMPONENTS
        )
        val circle: CircularBounds = CircularBounds.newInstance(
            currentPosition,
            50000.0
        )
        val includedTypes = listOf(
            PlaceTypes.RESTAURANT,
            PlaceTypes.CAFE,
            PlaceTypes.MEAL_TAKEAWAY
        )
        val includedPrimaryTypes = listOf(
            "mexican_restaurant"
        )

        val searchNearbyRequest: SearchNearbyRequest =
            SearchNearbyRequest.builder(circle, placeFields)
                .setIncludedPrimaryTypes(includedPrimaryTypes)
                .setIncludedTypes(includedTypes)
                .setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
                .setMaxResultCount(20)
                .build()

        try {
            placesClient.searchNearby(searchNearbyRequest)
                .addOnSuccessListener({ response ->
                    Log.d("Places", "Response: $response")
                    response.places.let { places.addAll(it) }
                })
                .addOnFailureListener({ exception ->
                    Log.e("Places", "Error occurred: $exception")
                }).await()
        } catch (e: Exception) {
            // Handle exception
        }
    }

    Column {
        Spacer(modifier = Modifier.height(statusBarHeight()))
        Box {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = MapType.HYBRID,
                    isTrafficEnabled = true
                )
            ) {
                Marker(
                    state = markerState,
                    title = "You",
                    snippet = "You are hungry here",
                    draggable = true
                )
                places.forEach { place ->
                    place.location?.let { latLng ->
                        CustomMarker(
                            place = place,
                            latLng = latLng,
                            currentPosition = currentPosition
                        )
                    }
                }
            }
            Button(
                onClick = onBackPressed,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .wrapContentSize(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalExColorScheme.current.extra.iconBackground
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 12.dp,
                ),
                shape = RectangleShape,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LocalExColorScheme.current.extra.iconTint
                )
            }
        }
    }
}

@Composable
fun CustomMarker(place: Place, latLng: LatLng, currentPosition: LatLng) {
    val context = LocalContext.current
    val address = readablePlaceAddress(place.addressComponents)
    val shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 0.dp)

    MarkerInfoWindowComposable(
        state = rememberUpdatedMarkerState(position = latLng),
        title = place.displayName,
        snippet = address,
        anchor = Offset(0f, 1f),
        infoContent = {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LocalExColorScheme.current.extra.iconBackground)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = false
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = place.displayName ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = address)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    shape = CircleShape,
                    modifier = Modifier.height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    ),
                    onClick = {
                        val uri =
                            "https://www.google.com/maps/dir/?api=1&travelmode=driving&origin=${currentPosition.latitude},${currentPosition.longitude}&destination=${latLng.latitude},${latLng.longitude}".toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)

                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Text("Let's go!", fontSize = 18.dp.value.sp)
                }
            }
        }
    ) {
        Image(
            painter = painterResource(id = R.drawable.burrito_icon),
            contentDescription = place.displayName,
            modifier = Modifier
                .size(60.dp)
                .border(6.dp, LocalExColorScheme.current.extra.iconOutline, shape = shape),
        )
    }
}

@Composable
fun RationaleAlert(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties()
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "We need location permissions to use this app",
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("OK")
                }
            }
        }
    }
}

private suspend fun CameraPositionState.centerOnLocation(
    location: LatLng
) = animate(
    update = CameraUpdateFactory.newLatLngZoom(
        location,
        14f
    ),
    durationMs = 1500
)
