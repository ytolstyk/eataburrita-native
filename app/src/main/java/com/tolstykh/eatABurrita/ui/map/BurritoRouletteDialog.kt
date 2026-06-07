package com.tolstykh.eatABurrita.ui.map

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.tolstykh.eatABurrita.helpers.distanceBetweenInMiles
import com.tolstykh.eatABurrita.ui.components.EmojiRainCanvas
import kotlin.math.abs
import kotlin.math.roundToInt

private val ITEM_HEIGHT = 48.dp
// Winner is placed at index 31; 4 padding items follow so the reel shows context on both sides.
private const val WINNER_INDEX = 31

@Composable
fun BurritoRouletteDialog(
    places: List<Place>,
    currentPosition: LatLng,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var spinKey by remember { mutableIntStateOf(0) }
    var winner by remember { mutableStateOf<Place?>(null) }
    var isLanded by remember { mutableStateOf(false) }

    val reel = remember(spinKey) { mutableStateListOf<String>() }
    val scrollPos = remember(spinKey) { Animatable(0f) }

    val density = LocalDensity.current
    val itemHeightPx = with(density) { ITEM_HEIGHT.toPx() }

    LaunchedEffect(spinKey) {
        isLanded = false
        val picked = places.random()
        winner = picked
        val namePool = places.map { it.displayName ?: "Restaurant" }
        val winnerName = picked.displayName ?: "Restaurant"

        reel.clear()
        repeat(WINNER_INDEX) { reel.add(namePool.random()) }
        reel.add(winnerName)
        repeat(4) { reel.add(namePool.random()) }

        // Three-phase deceleration matching 60 / 120 / 250 ms-per-item cadence,
        // then a bouncy spring snap onto the winner.
        scrollPos.animateTo(15f, tween(960, easing = LinearEasing))
        scrollPos.animateTo(25f, tween(1200, easing = LinearEasing))
        scrollPos.animateTo(30f, tween(1250, easing = LinearEasing))
        scrollPos.animateTo(
            WINNER_INDEX.toFloat(),
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
        isLanded = true
    }

    val winnerScale by animateFloatAsState(
        targetValue = if (isLanded) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "winnerScale",
    )

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "🎰 Burrito Roulette",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(16.dp))

                    // Slot-machine reel window — fixed 3-item height, items scroll through.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ITEM_HEIGHT * 3)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (reel.isNotEmpty()) {
                            val currentPos = scrollPos.value
                            val baseIndex = currentPos.roundToInt()

                            (-3..3).forEach { offset ->
                                val itemIndex = baseIndex + offset
                                if (itemIndex < 0 || itemIndex >= reel.size) return@forEach

                                // relativePos > 0 → item is below center (already scrolled past)
                                // relativePos < 0 → item is above center (arriving soon)
                                val relativePos = currentPos - itemIndex
                                val yOffsetPx = (relativePos * itemHeightPx).roundToInt()
                                val dist = abs(relativePos)

                                // Opacity falls off linearly beyond the center item.
                                val alpha = when {
                                    dist <= 0.5f -> 1f
                                    dist <= 1.5f -> 1f - (dist - 0.5f) * 0.65f
                                    dist <= 2.5f -> 0.35f * (2.5f - dist)
                                    else -> 0f
                                }.coerceIn(0f, 1f)

                                if (alpha > 0.01f) {
                                    Text(
                                        text = reel[itemIndex],
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset { IntOffset(0, yOffsetPx) }
                                            .alpha(alpha),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (dist < 0.5f && isLanded) FontWeight.Bold else FontWeight.Normal,
                                        color = colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            // Selection lines bracketing the center item.
                            val lineAlpha = if (isLanded) 0.5f else 0.25f
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .offset(y = -ITEM_HEIGHT / 2)
                                    .background(colorScheme.primary.copy(alpha = lineAlpha)),
                            )
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .offset(y = ITEM_HEIGHT / 2)
                                    .background(colorScheme.primary.copy(alpha = lineAlpha)),
                            )
                        }
                    }

                    if (isLanded) {
                        val place = winner
                        if (place != null) {
                            Spacer(Modifier.height(12.dp))
                            Column(
                                modifier = Modifier.graphicsLayer {
                                    scaleX = winnerScale
                                    scaleY = winnerScale
                                },
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "🏆 Your burrito awaits!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                place.rating?.let { rating ->
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = colorScheme.primary,
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = "%.1f".format(rating),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colorScheme.onSurface,
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${distanceBetweenInMiles(currentPosition, place.location)} miles away",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(16.dp))
                                Row {
                                    Button(
                                        onClick = {
                                            val loc = place.location
                                            val name = place.displayName
                                            val uriString = when {
                                                loc != null -> "https://www.google.com/maps/dir/?api=1&travelmode=driving&origin=${currentPosition.latitude},${currentPosition.longitude}&destination=${loc.latitude},${loc.longitude}"
                                                name != null -> "https://www.google.com/maps/dir/?api=1&travelmode=driving&destination=${Uri.encode(name)}"
                                                else -> null
                                            }
                                            uriString?.let {
                                                val intent = Intent(Intent.ACTION_VIEW, it.toUri())
                                                if (intent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(intent)
                                                }
                                            }
                                            onDismiss()
                                        },
                                    ) {
                                        Text("🗺️ Navigate!")
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedButton(onClick = { spinKey++ }) {
                                        Text("Try Again")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }

            if (isLanded) {
                EmojiRainCanvas(
                    modifier = Modifier.matchParentSize(),
                    key = "roulette_$spinKey",
                )
            }
        }
    }
}
