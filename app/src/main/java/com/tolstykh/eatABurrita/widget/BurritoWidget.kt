package com.tolstykh.eatABurrita.widget

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment as GlanceAlignment
import androidx.glance.layout.Box as GlanceBox
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight as GlanceFontWeight
import androidx.glance.text.Text as GlanceText
import androidx.glance.text.TextStyle as GlanceTextStyle
import androidx.glance.unit.ColorProvider
import com.tolstykh.eatABurrita.data.BurritoDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private val BurritoOrange = Color(0xFFFF8D03)
private val BurritoPurple = Color(0xFF9D00D6)

// GlanceAppWidget is not a supported Hilt injection target; @EntryPoint reaches
// the Hilt component from an arbitrary Context, as the Hilt docs prescribe.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun burritoDao(): BurritoDao
}

class BurritoWidget : GlanceAppWidget() {
    // Exact mode gives LocalSize the real widget dimensions at runtime so we can
    // size the circle to min(width, height) and guarantee a strict circle even
    // when the launcher assigns a non-square area for the 1x1 cell.
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .burritoDao()

        provideContent {
            val count by dao.getCount().collectAsState(initial = 0)
            BurritoWidgetContent(count = count)
        }
    }
}

@Composable
internal fun BurritoWidgetContent(count: Int) {
    val widgetSize = LocalSize.current
    val circleDp = (minOf(widgetSize.width.value, widgetSize.height.value) - 10f)
        .coerceAtLeast(40f).dp

    // Center the circle, then place the badge at the circle's bottom-right corner.
    // padding(bottom=4) shifts the center point up by 2dp.
    GlanceBox(
        modifier = GlanceModifier.fillMaxSize().padding(top = 8.dp),
        contentAlignment = GlanceAlignment.TopCenter,
    ) {
        // Wrapper matches circle size so BottomEnd lands on the circle's corner.
        GlanceBox(
            modifier = GlanceModifier.size(circleDp),
            contentAlignment = GlanceAlignment.BottomEnd,
        ) {
            GlanceBox(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(200.dp)
                    .background(ColorProvider(BurritoOrange))
                    .clickable(actionRunCallback<LogBurritoAction>()),
                contentAlignment = GlanceAlignment.Center,
            ) {
                GlanceText(
                    text = "Eat!",
                    style = GlanceTextStyle(
                        fontSize = 16.sp,
                        fontWeight = GlanceFontWeight.Bold,
                        color = ColorProvider(Color.White),
                    ),
                )
            }

            // Badge rendered on top at bottom-right corner of the circle
            GlanceBox(
                modifier = GlanceModifier
                    .size(26.dp)
                    .cornerRadius(13.dp)
                    .background(ColorProvider(BurritoPurple)),
                contentAlignment = GlanceAlignment.Center,
            ) {
                GlanceText(
                    text = if (count > 99) "99+" else "$count",
                    style = GlanceTextStyle(
                        fontSize = 11.sp,
                        fontWeight = GlanceFontWeight.Bold,
                        color = ColorProvider(Color.White),
                    ),
                )
            }
        }
    }
}

// Standard Compose @Preview — Kotlin resolves padding/size/etc by receiver type
// (Modifier vs GlanceModifier) so both import sets coexist without conflict.
@Preview(showBackground = false, widthDp = 110, heightDp = 110, name = "Burrito Widget 1x1")
@Composable
private fun BurritoWidgetPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().padding(top = 8.dp), contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier.size(100.dp), // min(110, 110) - 10
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFF8D03), shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Eat!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(26.dp)
                        .background(Color(0xFF9D00D6), shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "42",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}
