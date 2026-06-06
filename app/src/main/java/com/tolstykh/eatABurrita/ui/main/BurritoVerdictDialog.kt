package com.tolstykh.eatABurrita.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.tolstykh.eatABurrita.helpers.getBurritoVerdictShareMessage

@Composable
fun BurritoVerdictDialog(
    verdictState: TimeScreenViewModel.BurritoVerdictState,
    photoUri: Uri?,
    onAddEntry: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (verdictState) {
        is TimeScreenViewModel.BurritoVerdictState.Classifying -> {
            Dialog(onDismissRequest = {}) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colorScheme.surface,
                    modifier = Modifier.width(300.dp).wrapContentHeight(),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PhotoPreview(photoUri)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Analyzing your burrito...",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        is TimeScreenViewModel.BurritoVerdictState.Verdict -> {
            val confidence = verdictState.confidence
            val percent = (confidence * 100).toInt()

            val title: String
            val message: String
            val showAddButton: Boolean
            when {
                confidence >= 0.70f -> {
                    title = "Confirmed Burrito!"
                    message = "That is clearly a burrito. +1 logged."
                    showAddButton = true
                }
                confidence >= 0.40f -> {
                    title = "Burrito confidence: $percent%"
                    message = "We'll allow it."
                    showAddButton = true
                }
                else -> {
                    title = "That's not a burrito."
                    message = "Nice try."
                    showAddButton = false
                }
            }

            val context = LocalContext.current

            Dialog(onDismissRequest = onDismiss) {
                Surface(
                    modifier = Modifier.width(300.dp).wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    color = colorScheme.surface,
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        PhotoPreview(photoUri)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(title, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = {
                                val text = getBurritoVerdictShareMessage(confidence)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(intent, null))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share result")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = onDismiss) { Text("Dismiss") }
                            if (showAddButton) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = onAddEntry) { Text("Log Burrito") }
                            }
                        }
                    }
                }
            }
        }

        is TimeScreenViewModel.BurritoVerdictState.Failure -> {
            Dialog(onDismissRequest = onDismiss) {
                Surface(
                    modifier = Modifier.width(300.dp).wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    color = colorScheme.surface,
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        PhotoPreview(photoUri)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Scan failed", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Couldn't analyze the photo. Try again.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Button(onClick = onDismiss) { Text("OK") }
                        }
                    }
                }
            }
        }

        TimeScreenViewModel.BurritoVerdictState.None -> Unit
    }
}

@Composable
private fun PhotoPreview(photoUri: Uri?) {
    if (photoUri == null) return
    AsyncImage(
        model = photoUri,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp)),
    )
}
