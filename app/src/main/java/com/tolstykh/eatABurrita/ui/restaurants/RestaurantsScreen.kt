package com.tolstykh.eatABurrita.ui.restaurants

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tolstykh.eatABurrita.data.RestaurantNote
import com.tolstykh.eatABurrita.ui.components.RestaurantNoteDialog
import com.tolstykh.eatABurrita.ui.components.RestaurantNoteTarget

@Composable
fun RestaurantsScreen(
    viewModel: RestaurantsViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var editingPlaceId by rememberSaveable { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Restaurants", style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.height(8.dp))
            }
            HorizontalDivider()

            if (notes.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp),
                    ) {
                        Text(
                            "No saved restaurants yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Open the map and tap ✏ on a restaurant to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = onNavigateToMap) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Open Map")
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    notes.forEach { note ->
                        RestaurantNoteRow(
                            note = note,
                            onEdit = { editingPlaceId = note.placeId },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    // Edit dialog
    editingPlaceId?.let { pid ->
        val note = notes.find { it.placeId == pid }
        if (note != null) {
            RestaurantNoteDialog(
                target = RestaurantNoteTarget.FromList(note),
                onSave = { draft ->
                    viewModel.upsert(draft)
                    editingPlaceId = null
                },
                onDelete = { toDelete ->
                    viewModel.delete(toDelete)
                    editingPlaceId = null
                },
                onDismiss = { editingPlaceId = null },
            )
        } else {
            editingPlaceId = null
        }
    }
}

@Composable
private fun RestaurantNoteRow(
    note: RestaurantNote,
    onEdit: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                note.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            // Stars display
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= note.personalRating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (i <= note.personalRating) colorScheme.primary else colorScheme.outlineVariant,
                    )
                }
            }
            if (note.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    note.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (note.hideFromMap) {
                Spacer(Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text("Hidden", style = MaterialTheme.typography.labelSmall) },
                    icon = {
                        Icon(
                            Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = colorScheme.errorContainer,
                        labelColor = colorScheme.onErrorContainer,
                        iconContentColor = colorScheme.onErrorContainer,
                    ),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        }
        IconButton(onClick = {
            val uri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&travelmode=driving" +
                    "&destination=${Uri.encode(note.name)}" +
                    "&destination_place_id=${note.placeId}"
            )
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        }) {
            Icon(Icons.Default.Navigation, contentDescription = "Navigate")
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit notes")
        }
    }
}
