package com.tolstykh.eatABurrita.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tolstykh.eatABurrita.data.RestaurantNote

@Composable
fun RestaurantNoteDialog(
    target: RestaurantNoteTarget,
    onSave: (RestaurantNote) -> Unit,
    onDelete: (RestaurantNote) -> Unit,
    onDismiss: () -> Unit,
) {
    val (placeId, name, existingNote) = when (target) {
        is RestaurantNoteTarget.FromMap -> Triple(
            target.place.id ?: return,
            target.place.displayName ?: target.existingNote?.name ?: "",
            target.existingNote,
        )
        is RestaurantNoteTarget.FromList -> Triple(
            target.existingNote.placeId,
            target.existingNote.name,
            target.existingNote,
        )
    }

    var rating by rememberSaveable { mutableIntStateOf(existingNote?.personalRating ?: 0) }
    var notesText by rememberSaveable { mutableStateOf(existingNote?.notes ?: "") }
    var hideFromMap by rememberSaveable { mutableStateOf(existingNote?.hideFromMap ?: false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    if (showDeleteConfirm && existingNote != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete notes?") },
            text = { Text("Remove your saved notes for \"$name\"?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete(existingNote)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.wrapContentWidth().wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(name, style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(16.dp))

                // Star picker
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = if (rating == i) 0 else i },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i star",
                                tint = if (i <= rating) colorScheme.primary else colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (rating == 0) {
                    Text(
                        "Tap to rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { hideFromMap = !hideFromMap },
                ) {
                    Checkbox(
                        checked = hideFromMap,
                        onCheckedChange = { hideFromMap = it },
                    )
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colorScheme.onSurfaceVariant,
                    )
                    Text(
                        " Do not show on map",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (existingNote != null) {
                        TextButton(onClick = { showDeleteConfirm = true }) {
                            Text("Delete", color = colorScheme.error)
                        }
                    } else {
                        Spacer(Modifier)
                    }
                    Row {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Button(
                            onClick = {
                                onSave(
                                    RestaurantNote(
                                        placeId = placeId,
                                        name = name,
                                        personalRating = rating,
                                        notes = notesText,
                                        hideFromMap = hideFromMap,
                                    )
                                )
                            }
                        ) { Text("Save") }
                    }
                }
            }
        }
    }
}
