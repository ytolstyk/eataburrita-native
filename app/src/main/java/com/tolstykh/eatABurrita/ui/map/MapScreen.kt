package com.tolstykh.eatABurrita.ui.map

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.text.font.FontWeight
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.MapStyleOptions
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.tolstykh.eatABurrita.BuildConfig
import com.tolstykh.eatABurrita.R
import com.tolstykh.eatABurrita.helpers.distanceBetweenInMiles
import com.tolstykh.eatABurrita.helpers.statusBarHeight
import com.tolstykh.eatABurrita.location.hasLocationPermission
import com.tolstykh.eatABurrita.dateFromMilliseconds
import com.tolstykh.eatABurrita.readablePlaceAddress
import com.tolstykh.eatABurrita.ui.theme.LocalExColorScheme
import com.tolstykh.eatABurrita.ui.theme.extendedLight
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
    val placeStats by viewModel.placeStats.collectAsStateWithLifecycle()

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
                    val cameraState = rememberCameraPositionState {
                        viewModel.lastCameraPosition?.let { position = it }
                    }

                    LaunchedEffect(key1 = location) {
                        if (viewModel.lastCameraPosition == null) {
                            location?.let {
                                cameraState.centerOnLocation(it)
                            }
                        }
                    }

                    LaunchedEffect(cameraState.position) {
                        viewModel.lastCameraPosition = cameraState.position
                    }

                    location?.let { currentLoc ->
                        FullMapView(
                            currentPosition = currentLoc,
                            cameraState = cameraState,
                            onBackPressed = onBackPressed,
                            placeStats = placeStats,
                            getBurritoCount = viewModel::getBurritoCountForPlace,
                            getPlaceStats = viewModel::getPlaceStatsForPlace,
                        )
                    }
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
    placeStats: Map<String, MapScreenViewModel.PlaceStats> = emptyMap(),
    getBurritoCount: (Place, Map<String, MapScreenViewModel.PlaceStats>) -> Int = { _, _ -> 0 },
    getPlaceStats: (Place, Map<String, MapScreenViewModel.PlaceStats>) -> MapScreenViewModel.PlaceStats? = { _, _ -> null },
) {
    val marker = LatLng(currentPosition.latitude, currentPosition.longitude)
    val markerState = remember {
        MarkerState(position = marker)
    }
    val places = remember { mutableStateListOf<Place>() }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var showLayersMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val placesClient: PlacesClient = remember {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context, BuildConfig.MAPS_API_KEY)
        }

        Places.createClient(context)
    }

    LaunchedEffect(selectedPlace) {
        selectedPlace?.location?.let { loc ->
            cameraState.animate(CameraUpdateFactory.newLatLngZoom(loc, 16f), 350)
        }
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
                .addOnSuccessListener{ response ->
                    Log.d("Places", "Response: $response")
                    response.places.let { places.addAll(it) }
                }
                .addOnFailureListener{ exception ->
                    Log.e("Places", "Error occurred: $exception")
                }.await()
        } catch (e: Exception) {
            Log.e("Places", "Error occurred: $e")
        }
    }

    val scope = rememberCoroutineScope()
    val isDarkMode = colorScheme.background.luminance() < 0.5f
    val mapStyleOptions = if (isDarkMode) {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
    } else null

    Column {
        Spacer(modifier = Modifier.height(statusBarHeight()))
        Box {
            GoogleMap(
                onMapLoaded = {},
                onMapClick = { selectedPlace = null },
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = mapType,
                    isTrafficEnabled = true,
                    mapStyleOptions = if (mapType == MapType.NORMAL) mapStyleOptions else null,
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
                            burritoCount = getBurritoCount(place, placeStats),
                            onPlaceSelected = { selectedPlace = it }
                        )
                    }
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = selectedPlace != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                selectedPlace?.let { place ->
                    val stats = getPlaceStats(place, placeStats)
                    PlaceBottomTray(
                        place = place,
                        currentPosition = currentPosition,
                        burritoCount = stats?.count ?: 0,
                        lastTimestampAtPlace = stats?.lastTimestamp,
                        onNavigate = run {
                            val location = place.location
                            val name = place.displayName
                            val uriString = when {
                                location != null -> "https://www.google.com/maps/dir/?api=1&travelmode=driving&origin=${currentPosition.latitude},${currentPosition.longitude}&destination=${location.latitude},${location.longitude}"
                                name != null -> "https://www.google.com/maps/dir/?api=1&travelmode=driving&destination=${Uri.encode(name)}"
                                else -> null
                            }
                            uriString?.let { s ->
                                val intent = Intent(Intent.ACTION_VIEW, s.toUri())
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    { context.startActivity(intent) }
                                } else null
                            }
                        }
                    )
                }
            }
            Button(
                onClick = onBackPressed,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(40.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extendedLight.extra.iconBackground.copy(alpha = 0.85f)
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 4.dp,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = extendedLight.extra.iconTint
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 104.dp)
            ) {
                Button(
                    onClick = { showLayersMenu = !showLayersMenu },
                    modifier = Modifier.size(40.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedLight.extra.iconBackground.copy(alpha = 0.85f)
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Map layers",
                        tint = extendedLight.extra.iconTint
                    )
                }
                androidx.compose.material3.DropdownMenu(
                    expanded = showLayersMenu,
                    onDismissRequest = { showLayersMenu = false }
                ) {
                    listOf(
                        MapType.NORMAL to "Normal",
                        MapType.SATELLITE to "Satellite",
                        MapType.HYBRID to "Hybrid",
                        MapType.TERRAIN to "Terrain"
                    ).forEach { (type, label) ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { mapType = type; showLayersMenu = false },
                            leadingIcon = if (mapType == type) ({
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }) else null
                        )
                    }
                }
            }
            Button(
                onClick = {
                    scope.launch {
                        val current = cameraState.position
                        cameraState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.builder()
                                    .target(current.target)
                                    .zoom(current.zoom)
                                    .bearing(0f)
                                    .tilt(0f)
                                    .build()
                            ),
                            durationMs = 400,
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 56.dp)
                    .size(40.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = extendedLight.extra.iconBackground.copy(alpha = 0.85f)
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 4.dp,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Reset orientation",
                    tint = extendedLight.extra.iconTint
                )
            }
        }
    }
}

@Composable
fun CustomMarker(
    place: Place,
    latLng: LatLng,
    burritoCount: Int = 0,
    onPlaceSelected: (Place) -> Unit,
) {
    val shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 0.dp)

    MarkerComposable(
        state = rememberUpdatedMarkerState(position = latLng),
        title = place.displayName,
        anchor = Offset(0f, 1f),
        onClick = { onPlaceSelected(place); true }
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Image(
                painter = painterResource(id = R.drawable.burrito_icon),
                contentDescription = place.displayName,
                modifier = Modifier
                    .size(48.dp)
                    .border(3.dp, LocalExColorScheme.current.extra.iconOutline, shape = shape),
            )
            if (burritoCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 5.dp, y = (-5).dp)
                        .size(24.dp)
                        .background(colorScheme.error, shape = CircleShape)
                        .border(1.5.dp, colorScheme.onError, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (burritoCount > 9) "9+" else burritoCount.toString(),
                        color = colorScheme.onError,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceBottomTray(
    place: Place,
    currentPosition: LatLng,
    burritoCount: Int = 0,
    lastTimestampAtPlace: Long? = null,
    onNavigate: (() -> Unit)?,
) {
    val address = readablePlaceAddress(place.addressComponents)
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 16.dp,
        color = colorScheme.primary,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = place.displayName ?: "",
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${distanceBetweenInMiles(currentPosition, place.location)} miles away",
                color = colorScheme.onPrimary,
            )
            if (address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onPrimary,
                )
            }
            if (burritoCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$burritoCount ${if (burritoCount == 1) "burrito" else "burritos"} eaten here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onPrimary,
                )
                lastTimestampAtPlace?.let { ts ->
                    Text(
                        text = "Last visit: ${dateFromMilliseconds(ts)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onPrimary.copy(alpha = 0.8f),
                    )
                }
            }
            if (onNavigate != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigate,
                    shape = CircleShape,
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary)
                ) {
                    Text("Navigate", fontSize = 18.dp.value.sp, color = colorScheme.onSecondary)
                }
            }
        }
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
    durationMs = 600
)
