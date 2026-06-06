package com.tolstykh.eatABurrita.ui.share

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.tolstykh.eatABurrita.helpers.BurritoPersonality
import com.tolstykh.eatABurrita.helpers.computeBurritoPersonality
import com.tolstykh.eatABurrita.helpers.getRandomMessageWithStats
import com.tolstykh.eatABurrita.ui.main.TimeScreenData
import com.tolstykh.eatABurrita.ui.theme.Orange
import com.tolstykh.eatABurrita.ui.theme.Purple
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun BurritoShareCardContent(
    data: TimeScreenData,
    modifier: Modifier = Modifier,
) {
    val personality = remember(data.hourOfDayCounts, data.dayOfWeekCounts) {
        computeBurritoPersonality(data.hourOfDayCounts, data.dayOfWeekCounts)
    }
    val gradient = Brush.verticalGradient(listOf(Orange, Purple))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .padding(28.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🌯 Eat-a-Burrita",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Annual count
            Text(
                text = if (data.yearCount > 0) "${data.yearCount}" else "${data.burritoCount}",
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 76.sp,
            )
            Text(
                text = if (data.yearCount > 0) "burritos this year" else "burritos total",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Favorite location
            if (data.favoritePlaceName != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍", fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = data.favoritePlaceName,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Streak chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StreakChip(label = "🔥 Streak", value = "${data.currentStreak} days")
                StreakChip(label = "🏆 Best", value = "${data.bestStreak} days")
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            // Personality
            Text(
                text = personality.emoji,
                fontSize = 32.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = personality.label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = personality.description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "eat-a-burrita",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.45f),
                letterSpacing = 1.5.sp,
            )
        }
    }
}

@Composable
private fun StreakChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
fun BurritoShareCardDialog(
    data: TimeScreenData,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var cardBounds by remember { mutableStateOf<android.graphics.RectF?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Share your stats",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                BurritoShareCardContent(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            val rect = coords.boundsInWindow()
                            cardBounds = android.graphics.RectF(rect.left, rect.top, rect.right, rect.bottom)
                        },
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val bounds = cardBounds ?: return@Button
                        if (isCapturing) return@Button
                        isCapturing = true
                        scope.launch {
                            try {
                                val bitmap = captureRegion(view, bounds)
                                shareCardBitmap(context, bitmap)
                            } finally {
                                isCapturing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCapturing,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                ) {
                    Text(if (isCapturing) "Capturing…" else "Share as Image")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val text = getRandomMessageWithStats(
                            burritoCount = data.burritoCount,
                            lastTimestamp = data.lastTimestamp,
                            dailyCounts = data.dailyCounts,
                            favoritePlaceName = data.favoritePlaceName,
                            favoritePlaceLat = data.favoritePlaceLat,
                            favoritePlaceLng = data.favoritePlaceLng,
                            lastPlaceName = data.lastPlaceName,
                            lastPlaceLat = data.lastPlaceLat,
                            lastPlaceLng = data.lastPlaceLng,
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Share as Text")
                }
            }
        }
    }
}

private suspend fun captureRegion(view: android.view.View, boundsF: android.graphics.RectF): Bitmap {
    val bounds = Rect(
        boundsF.left.toInt(),
        boundsF.top.toInt(),
        boundsF.right.toInt(),
        boundsF.bottom.toInt(),
    )
    val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
    return suspendCancellableCoroutine { cont ->
        PixelCopy.request(
            (view.context as Activity).window,
            bounds,
            bitmap,
            { result ->
                if (result == PixelCopy.SUCCESS) {
                    cont.resume(bitmap)
                } else {
                    cont.resumeWithException(Exception("PixelCopy failed: $result"))
                }
            },
            Handler(Looper.getMainLooper()),
        )
    }
}

private fun shareCardBitmap(context: Context, bitmap: Bitmap) {
    val shareDir = File(context.cacheDir, "share").also { it.mkdirs() }
    val file = File(shareDir, "burrito_card.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
