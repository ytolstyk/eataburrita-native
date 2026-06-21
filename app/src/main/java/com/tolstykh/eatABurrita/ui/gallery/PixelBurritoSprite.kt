package com.tolstykh.eatABurrita.ui.gallery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas as GraphicsCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private object PixelCode {
    const val TRANSPARENT   = 0
    const val TORTILLA_BODY = 1
    const val FILL_A        = 2
    const val FILL_B        = 3
    const val TORTILLA_CAP  = 4
    const val HIGHLIGHT     = 5
}

private val TORTILLA_PALETTES = listOf(
    Pair(Color(0xFFE8C878), Color(0xFFB89048)),
    Pair(Color(0xFFF0D8A0), Color(0xFFD0B070)),
    Pair(Color(0xFFD0B060), Color(0xFF907030)),
    Pair(Color(0xFFC8A058), Color(0xFF885820)),
)

private val FILLING_PALETTE = listOf(
    Color(0xFF8B4513),
    Color(0xFFA0522D),
    Color(0xFF228B22),
    Color(0xFFDAA520),
    Color(0xFFDC143C),
    Color(0xFF6B8E23),
    Color(0xFFCD853F),
    Color(0xFF5B8C5A),
)

private val HIGHLIGHT_COLOR = Color(0xFFF8F0D0)

// 16×16 pixel grid — codes map via PixelCode constants above
private val SPRITE = arrayOf(
    intArrayOf(0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0),
    intArrayOf(0, 0, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 0, 0),
    intArrayOf(0, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 0),
    intArrayOf(4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4),
    intArrayOf(4, 1, 5, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 4),
    intArrayOf(4, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 4),
    intArrayOf(4, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 4),
    intArrayOf(4, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 4),
    intArrayOf(4, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 4),
    intArrayOf(4, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 4),
    intArrayOf(4, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 4),
    intArrayOf(4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4),
    intArrayOf(0, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 0),
    intArrayOf(0, 0, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 0, 0),
    intArrayOf(0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0),
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
)

internal data class BurritoSpriteColors(
    val body: Color,
    val cap: Color,
    val fillA: Color,
    val fillB: Color,
)

internal fun spriteColorsFor(seed: Long): BurritoSpriteColors {
    val rng = Random(seed)
    val palette = TORTILLA_PALETTES[rng.nextInt(TORTILLA_PALETTES.size)]
    val fillAIdx = rng.nextInt(FILLING_PALETTE.size)
    var fillBIdx = rng.nextInt(FILLING_PALETTE.size)
    while (fillBIdx == fillAIdx) fillBIdx = rng.nextInt(FILLING_PALETTE.size)
    val swap = rng.nextBoolean()
    val a = FILLING_PALETTE[fillAIdx]
    val b = FILLING_PALETTE[fillBIdx]
    return BurritoSpriteColors(palette.first, palette.second, if (swap) b else a, if (swap) a else b)
}

// Rasterizes the 16×16 sprite into an ImageBitmap once; pure and deterministic.
fun rasterizeSprite(seed: Long): ImageBitmap {
    val colors = spriteColorsFor(seed)
    val bmp = ImageBitmap(16, 16)
    val canvas = GraphicsCanvas(bmp)
    val paint = Paint()
    for (row in SPRITE.indices) {
        for (col in SPRITE[row].indices) {
            paint.color = when (SPRITE[row][col]) {
                PixelCode.TORTILLA_BODY -> colors.body
                PixelCode.FILL_A        -> colors.fillA
                PixelCode.FILL_B        -> colors.fillB
                PixelCode.TORTILLA_CAP  -> colors.cap
                PixelCode.HIGHLIGHT     -> HIGHLIGHT_COLOR
                else                    -> continue
            }
            canvas.drawRect(
                Rect(col.toFloat(), row.toFloat(), (col + 1).toFloat(), (row + 1).toFloat()),
                paint,
            )
        }
    }
    return bmp
}

fun spriteBackground(seed: Long): Color = spriteColorsFor(seed).fillA.copy(alpha = 0.15f)

@Composable
fun PixelBurritoSprite(bitmap: ImageBitmap, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawImage(
            image = bitmap,
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
            filterQuality = FilterQuality.None,
        )
    }
}

// Full gallery tile: base + tinted background + sprite + optional ordinal label.
// Callers are responsible for clip/shape/clickable on the modifier.
@Composable
fun PixelBurritoTile(
    bitmap: ImageBitmap,
    background: Color,
    label: String? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.fillMaxSize().background(background))
        PixelBurritoSprite(
            bitmap = bitmap,
            modifier = Modifier.fillMaxSize(0.7f),
        )
        if (label != null) {
            Text(
                text = label,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp),
            )
        }
    }
}
