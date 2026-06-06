package com.tolstykh.eatABurrita.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas

internal val confettiColors = listOf(
    Color(0xFFFF8D03),
    Color(0xFF9D00D6),
    Color(0xFF2F61D6),
    Color(0xFFE53935),
    Color(0xFF43A047),
    Color(0xFFFFB300),
    Color(0xFFEC407A),
)

internal data class ParticleInit(
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
internal fun ConfettiCanvas(
    modifier: Modifier = Modifier,
    key: String,
    particleCount: Int = 40,
    durationMs: Float = 4000f,
    drawEmoji: Boolean = false,
) {
    val rng = remember(key) { java.util.Random() }
    val particles = remember(key) {
        List(particleCount) {
            ParticleInit(
                x0 = rng.nextFloat(),
                y0 = -rng.nextFloat() * 0.8f,
                vx = (rng.nextFloat() - 0.5f) * 0.0007f,
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

    // Cached native Paint for emoji rendering — allocated once per key, not per frame
    val emojiPaint = if (drawEmoji) remember(key) {
        Paint().apply {
            isAntiAlias = true
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }
    } else null

    Canvas(
        modifier = modifier.graphicsLayer { alpha = canvasAlpha },
    ) {
        if (drawEmoji && emojiPaint != null) {
            emojiPaint.textSize = size.width * 0.07f
        }
        val t = elapsed
        val gravity = 0.00000005f
        particles.forEach { p ->
            val px = (p.x0 + p.vx * t) * size.width
            val py = (p.y0 + p.vy * t + 0.5f * gravity * t * t) * size.height
            if (py > size.height || py < -p.size) return@forEach

            if (drawEmoji && emojiPaint != null) {
                val emoji = if (p.isCircle) "🌯" else "🔥"
                drawContext.canvas.nativeCanvas.drawText(emoji, px, py, emojiPaint)
            } else {
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
}

@Composable
fun EmojiRainCanvas(modifier: Modifier = Modifier, key: String) {
    ConfettiCanvas(
        modifier = modifier,
        key = key,
        particleCount = 32,
        durationMs = 3500f,
        drawEmoji = true,
    )
}

@Composable
fun StreakConfettiCanvas(modifier: Modifier = Modifier, key: String) {
    ConfettiCanvas(
        modifier = modifier,
        key = key,
        particleCount = 60,
        durationMs = 4000f,
        drawEmoji = false,
    )
}
