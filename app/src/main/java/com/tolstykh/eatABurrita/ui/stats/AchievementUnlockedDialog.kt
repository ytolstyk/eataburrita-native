package com.tolstykh.eatABurrita.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.withFrameNanos

private val confettiColors = listOf(
    Color(0xFFFF8D03),
    Color(0xFF9D00D6),
    Color(0xFF2F61D6),
    Color(0xFFE53935),
    Color(0xFF43A047),
    Color(0xFFFFB300),
    Color(0xFFEC407A),
)

private data class ParticleInit(
    val x0: Float,
    val y0: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val isCircle: Boolean,
    val rotation0: Float,
    val rotSpeed: Float,
)

@Composable
fun AchievementUnlockedDialog(
    achievements: List<Achievement>,
    onDismiss: () -> Unit,
) {
    if (achievements.isEmpty()) return

    var currentIndex by remember(achievements) { mutableIntStateOf(0) }
    val achievement = achievements[currentIndex]
    val hasNext = currentIndex < achievements.lastIndex

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surface,
            modifier = Modifier
                .width(300.dp)
                .wrapContentHeight(),
        ) {
            Box {
                // Content
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(achievement.emoji, fontSize = 48.sp)
                    Text(
                        text = "Achievement Unlocked!",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = achievement.name,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = colorScheme.onSurface.copy(alpha = 0.65f),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) { Text("Close") }
                        if (hasNext) {
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { currentIndex++ }) {
                                val remaining = achievements.size - currentIndex - 1
                                Text("Next ($remaining more)")
                            }
                        }
                    }
                }
                // Confetti overlays the full dialog, falling from top to bottom
                ConfettiCanvas(
                    modifier = Modifier.matchParentSize(),
                    key = achievement.id,
                )
            }
        }
    }
}

@Composable
private fun ConfettiCanvas(modifier: Modifier = Modifier, key: String) {
    val rng = remember(key) { java.util.Random() }
    // Store only immutable initial conditions; positions computed analytically from elapsed time
    val particles = remember(key) {
        List(40) {
            ParticleInit(
                x0 = rng.nextFloat(),
                // Stagger start positions above canvas so they enter at different times
                y0 = -rng.nextFloat() * 0.8f,
                vx = (rng.nextFloat() - 0.5f) * 0.0007f,
                // vy scaled for full-dialog canvas: 0.0003–0.001 fraction/ms gives the same
                // physical dp/ms speed as the previous 100dp-tall canvas
                vy = rng.nextFloat() * 0.0007f + 0.0003f,
                color = confettiColors[rng.nextInt(confettiColors.size)],
                size = rng.nextFloat() * 8f + 4f,
                isCircle = rng.nextBoolean(),
                rotation0 = rng.nextFloat() * 360f,
                rotSpeed = (rng.nextFloat() - 0.5f) * 0.5f,
            )
        }
    }

    var elapsed by remember(key) { mutableFloatStateOf(0f) }
    // Extended to match the larger canvas — slowest particles (vy≈0.0003) need ~4 s to cross
    val durationMs = 4000f

    // Only tick elapsed — positions derived analytically below
    LaunchedEffect(key) {
        var lastFrameNanos = 0L
        while (elapsed < durationMs) {
            withFrameNanos { frameNanos ->
                val dt = if (lastFrameNanos == 0L) 16f
                else (frameNanos - lastFrameNanos) / 1_000_000f
                lastFrameNanos = frameNanos
                elapsed = (elapsed + dt).coerceAtMost(durationMs)
            }
        }
    }

    val canvasAlpha = if (elapsed > durationMs - 600f) {
        1f - (elapsed - (durationMs - 600f)) / 600f
    } else 1f

    Canvas(
        modifier = modifier.graphicsLayer { alpha = canvasAlpha },
    ) {
        val t = elapsed
        // Gentle gravity: 0.00000005 → adds ~0.4 canvas-heights at 4000 ms
        val gravity = 0.00000005f
        particles.forEach { p ->
            val px = (p.x0 + p.vx * t) * size.width
            val py = (p.y0 + p.vy * t + 0.5f * gravity * t * t) * size.height
            if (py > size.height || py < -p.size) return@forEach
            val rot = p.rotation0 + p.rotSpeed * t
            withTransform({
                rotate(degrees = rot, pivot = Offset(px, py))
            }) {
                if (p.isCircle) {
                    drawCircle(color = p.color, radius = p.size, center = Offset(px, py))
                } else {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(px - p.size / 2f, py - p.size / 2f),
                        size = Size(p.size, p.size * 1.6f),
                    )
                }
            }
        }
    }
}
