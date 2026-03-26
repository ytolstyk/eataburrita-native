package com.tolstykh.eatABurrita.ui.main

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.tolstykh.eatABurrita.BuildConfig
import com.tolstykh.eatABurrita.helpers.distanceBetweenInMiles
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun LocationPickerModal(
    currentLocation: LatLng?,
    hasLocationPermission: Boolean,
    onConfirm: (name: String?, lat: Double?, lng: Double?) -> Unit,
    onCancel: () -> Unit,
    onDontShowAgain: () -> Unit,
) {
    val context = LocalContext.current
    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context, BuildConfig.MAPS_API_KEY)
        }
        Places.createClient(context)
    }

    val nearbyPlaces = remember { mutableStateListOf<Place>() }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var manualName by remember { mutableStateOf("") }
    var dontShowAgain by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var searchText by remember { mutableStateOf("") }
    val searchPredictions = remember { mutableStateListOf<com.google.android.libraries.places.api.model.AutocompletePrediction>() }
    var skipNextSearch by remember { mutableStateOf(false) }
    var searchFieldWidth by remember { mutableStateOf(0) }

    LaunchedEffect(searchText) {
        if (skipNextSearch) {
            skipNextSearch = false
            return@LaunchedEffect
        }
        if (searchText.isBlank()) {
            searchPredictions.clear()
            return@LaunchedEffect
        }
        delay(300)
        try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(searchText)
                .setTypesFilter(listOf("establishment"))
                .build()
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    searchPredictions.clear()
                    searchPredictions.addAll(response.autocompletePredictions.take(5))
                }
                .addOnFailureListener { e -> Log.e("LocationPicker", "Autocomplete failed", e) }
                .await()
        } catch (e: Exception) {
            Log.e("LocationPicker", "Autocomplete error", e)
        }
    }

    LaunchedEffect(hasLocationPermission, currentLocation) {
        if (hasLocationPermission && currentLocation != null) {
            isLoading = true
            try {
                val placeFields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION)
                val circle = CircularBounds.newInstance(currentLocation, 1609.0)
                val request = SearchNearbyRequest.builder(circle, placeFields)
                    .setIncludedTypes(listOf(PlaceTypes.RESTAURANT, PlaceTypes.CAFE, PlaceTypes.MEAL_TAKEAWAY))
                    .setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
                    .setMaxResultCount(10)
                    .build()
                placesClient.searchNearby(request)
                    .addOnSuccessListener { response -> nearbyPlaces.addAll(response.places) }
                    .addOnFailureListener { e -> Log.e("LocationPicker", "Places search failed", e) }
                    .await()
            } catch (e: Exception) {
                Log.e("LocationPicker", "Places search error", e)
            } finally {
                isLoading = false
            }
        }
    }

    Dialog(onDismissRequest = onCancel) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Where are you eating?", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))

                val density = LocalDensity.current
                Box(modifier = Modifier.fillMaxWidth().onGloballyPositioned { searchFieldWidth = it.size.width }) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search restaurant") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DropdownMenu(
                        expanded = searchPredictions.isNotEmpty(),
                        onDismissRequest = { searchPredictions.clear() },
                        modifier = Modifier.width(with(density) { searchFieldWidth.toDp() }),
                    ) {
                        searchPredictions.forEach { prediction ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = prediction.getPrimaryText(null).toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Text(
                                            text = prediction.getSecondaryText(null).toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                                onClick = {
                                    val placeRequest = FetchPlaceRequest.newInstance(
                                        prediction.placeId,
                                        listOf(Place.Field.DISPLAY_NAME, Place.Field.LOCATION),
                                    )
                                    placesClient.fetchPlace(placeRequest)
                                        .addOnSuccessListener { fetchResponse ->
                                            skipNextSearch = true
                                            selectedPlace = fetchResponse.place
                                            searchText = fetchResponse.place.displayName ?: ""
                                            searchPredictions.clear()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("LocationPicker", "FetchPlace failed", e)
                                        }
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!hasLocationPermission) {
                    Text(
                        "Location permission not granted. Enable it in Settings to see nearby restaurants.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualName,
                        onValueChange = { manualName = it },
                        label = { Text("Restaurant name (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Finding nearby restaurants…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                } else if (nearbyPlaces.isEmpty()) {
                    Text(
                        "No nearby restaurants found within 1 mile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        "Nearby restaurants:",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                        items(nearbyPlaces) { place ->
                            val isSelected = selectedPlace?.id == place.id
                            Surface(
                                color = if (isSelected) colorScheme.primaryContainer else colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPlace = if (isSelected) null else place },
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = place.displayName ?: "Unknown",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface,
                                    )
                                    val dist = distanceBetweenInMiles(currentLocation, place.location)
                                    if (dist.isNotEmpty()) {
                                        Text(
                                            text = "$dist mi",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = colorScheme.outlineVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { dontShowAgain = !dontShowAgain },
                ) {
                    Checkbox(checked = dontShowAgain, onCheckedChange = { dontShowAgain = it })
                    Text("Don't show this again", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                ) {
                    TextButton(onClick = {
                        if (dontShowAgain) onDontShowAgain()
                        onCancel()
                    }) { Text("Skip") }
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    val canConfirm = selectedPlace != null || !hasLocationPermission || nearbyPlaces.isEmpty()
                    Button(
                        onClick = {
                            if (dontShowAgain) onDontShowAgain()
                            if (selectedPlace != null) {
                                val p = selectedPlace!!
                                onConfirm(p.displayName, p.location?.latitude, p.location?.longitude)
                            } else if (!hasLocationPermission) {
                                onConfirm(manualName.trim().ifEmpty { null }, null, null)
                            } else {
                                onConfirm(null, null, null)
                            }
                        },
                        enabled = canConfirm,
                    ) {
                        Text("Confirm Location")
                    }
                }
            }
        }
    }
}
