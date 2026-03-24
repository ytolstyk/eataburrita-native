package com.tolstykh.eatABurrita.ui.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.tolstykh.eatABurrita.data.BurritoEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun LocationEditModal(
    entry: BurritoEntry,
    newTimestamp: Long,
    placesClient: PlacesClient,
    onConfirm: (BurritoEntry) -> Unit,
    onDismiss: () -> Unit,
    isNewEntry: Boolean = false,
) {
    var searchText by remember { mutableStateOf(entry.locationName ?: "") }
    val predictions = remember { mutableStateListOf<com.google.android.libraries.places.api.model.AutocompletePrediction>() }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(searchText) {
        if (searchText.isBlank()) {
            predictions.clear()
            return@LaunchedEffect
        }
        delay(300)
        isLoading = true
        try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(searchText)
                .setTypesFilter(listOf("establishment"))
                .build()
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    predictions.clear()
                    predictions.addAll(response.autocompletePredictions.take(5))
                }
                .addOnFailureListener { e -> Log.e("LocationEdit", "Autocomplete failed", e) }
                .await()
        } catch (e: Exception) {
            Log.e("LocationEdit", "Autocomplete error", e)
        } finally {
            isLoading = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    if (isNewEntry) "Add location" else "Edit location",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search restaurant") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (predictions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        LazyColumn {
                            items(predictions) { prediction ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val placeRequest = FetchPlaceRequest.newInstance(
                                                prediction.placeId,
                                                listOf(Place.Field.DISPLAY_NAME, Place.Field.LOCATION),
                                            )
                                            placesClient.fetchPlace(placeRequest)
                                                .addOnSuccessListener { fetchResponse ->
                                                    val place = fetchResponse.place
                                                    onConfirm(
                                                        entry.copy(
                                                            timestamp = newTimestamp,
                                                            locationName = place.displayName,
                                                            locationLat = place.location?.latitude,
                                                            locationLong = place.location?.longitude,
                                                        )
                                                    )
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("LocationEdit", "FetchPlace failed", e)
                                                }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                ) {
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
                                HorizontalDivider(thickness = 0.5.dp, color = colorScheme.outlineVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (entry.locationName != null) {
                    TextButton(
                        onClick = {
                            onConfirm(entry.copy(timestamp = newTimestamp, locationName = null, locationLat = null, locationLong = null))
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error),
                        modifier = Modifier.align(Alignment.Start),
                    ) {
                        Text("Clear location")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Button(
                    onClick = { onConfirm(entry.copy(timestamp = newTimestamp)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (isNewEntry) "Save without location" else "Keep existing location")
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
