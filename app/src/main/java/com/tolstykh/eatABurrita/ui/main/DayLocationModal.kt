package com.tolstykh.eatABurrita.ui.main

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.tolstykh.eatABurrita.dateFromMilliseconds

@Composable
fun DayLocationModal(
    data: TimeScreenViewModel.DayLocationData,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(20.dp).heightIn(max = 480.dp)) {
                Text(
                    "Burrito locations on ${data.dateLabel}",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = if (data.totalCount > data.entries.size)
                        "${data.totalCount} burritos (showing ${data.entries.size})"
                    else
                        "${data.totalCount} burrito${if (data.totalCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (data.entries.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(data.entries) { entry ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = entry.locationName ?: "Unknown location",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = dateFromMilliseconds(entry.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                    )
                                    val lat = entry.locationLat
                                    val lng = entry.locationLong
                                    if (lat != null && lng != null) {
                                        TextButton(
                                            onClick = {
                                                val name = entry.locationName ?: ""
                                                val uri = "geo:$lat,$lng?q=$lat,$lng($name)".toUri()
                                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                            },
                                        ) {
                                            Text("Navigate")
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = colorScheme.outlineVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Close")
                }
            }
        }
    }
}
