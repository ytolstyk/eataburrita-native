package com.tolstykh.eatABurrita.widget

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment as GlanceAlignment
import androidx.glance.layout.Box as GlanceBox
import androidx.glance.layout.Column as GlanceColumn
import androidx.glance.layout.Row as GlanceRow
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight as GlanceFontWeight
import androidx.glance.text.Text as GlanceText
import androidx.glance.text.TextStyle as GlanceTextStyle
import androidx.glance.unit.ColorProvider
import com.tolstykh.eatABurrita.MainActivity
import com.tolstykh.eatABurrita.R
import com.tolstykh.eatABurrita.data.DayString
import dagger.hilt.android.EntryPointAccessors
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private val StatsOrange  = Color(0xFFFF8D03)  // primary   — Eat!
private val StatsBlue    = Color(0xFF2f61d6)  // tertiary  — Map
private val StatsPurple  = Color(0xFF9D00D6)  // secondary — Share
private val StatsGreen   = Color(0xFF43A047)  //            Camera
private val WidgetBgColor  = Color(0xFF1C1C1E)
private val StatLabelColor = Color(0xFFAAAAAA)

private const val NUM_STATS = 5
private const val CYCLE_MS  = 5_000L

internal data class WidgetStat(val label: String, val value: String)

internal fun computeStatsData(
    count: Int,
    distinctDays: List<DayString>,
    totalCalories: Int,
): List<WidgetStat> {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val days = distinctDays.map { LocalDate.parse(it.day) }
    val daySet = days.toHashSet()

    var currentStreak = 0
    var d = today
    while (d in daySet) { currentStreak++; d = d.minusDays(1) }

    var bestStreak = 0
    var streak = 0
    var lastDay: LocalDate? = null
    days.forEach { day ->
        streak = if (lastDay == null || day == lastDay!!.plusDays(1)) streak + 1 else 1
        bestStreak = maxOf(bestStreak, streak)
        lastDay = day
    }

    val avgPerWeek = if (days.isEmpty()) 0f else {
        val weeksSince = ChronoUnit.WEEKS.between(days.first(), today).coerceAtLeast(1)
        count.toFloat() / weeksSince
    }

    return listOf(
        WidgetStat("TOTAL",    "$count burritos"),
        WidgetStat("STREAK",   "$currentStreak days"),
        WidgetStat("BEST",     "$bestStreak days"),
        WidgetStat("AVG/WEEK", String.format("%.1f", avgPerWeek)),
        WidgetStat("CALORIES", "${NumberFormat.getIntegerInstance().format(totalCalories)} cal"),
    )
}

internal fun launcherIconSizeDp(context: Context): Dp {
    val am = context.getSystemService(ActivityManager::class.java)
    val px = am?.launcherLargeIconSize ?: return 52.dp
    val density = context.resources.displayMetrics.density
    return (px / density).dp
}

class BurritoStatsWidget : GlanceAppWidget() {
    // Exact mode supplies LocalSize so we can cap circles to the actual cell height.
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .burritoDao()

        val iconSizeDp = launcherIconSizeDp(context)

        provideContent {
            val count       by dao.getCount().collectAsState(initial = 0)
            val distinctDays by dao.getDistinctDays().collectAsState(initial = emptyList())
            val totalCalories by dao.getTotalCalories().collectAsState(initial = 0)

            val stats    = computeStatsData(count, distinctDays, totalCalories)
            // Derive the current stat from wall time so any redraw always shows
            // the right stat — no DataStore writes needed.
            val statIndex = ((System.currentTimeMillis() / CYCLE_MS) % NUM_STATS).toInt()

            BurritoStatsWidgetContent(
                statIndex    = statIndex,
                stats        = stats,
                iconSizeDp   = iconSizeDp,
            )
        }
    }
}

@Composable
internal fun BurritoStatsWidgetContent(
    statIndex: Int,
    stats: List<WidgetStat>,
    iconSizeDp: Dp,
) {
    val context    = LocalContext.current
    val cellHeight = LocalSize.current.height

    // Cap circles at their initial 4-cell size (48dp) so any extra width from
    // resizing goes entirely to the stats info area, not to bigger buttons.
    val circleDp   = minOf(iconSizeDp, cellHeight - 8.dp, 48.dp).coerceAtLeast(36.dp)
    val pillHeight = (circleDp + 8.dp).coerceAtMost(cellHeight)

    val stat = stats.getOrElse(statIndex % stats.size) { stats.first() }

    // Each intent needs a unique action string so Android's PendingIntent system
    // treats them as distinct. Intents differing only in extras are considered
    // identical for matching purposes — the last one would silently overwrite the
    // others (FLAG_UPDATE_CURRENT), causing every button to open the camera.
    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        action = "com.tolstykh.eatABurrita.widget.OPEN_APP"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val openMapIntent = Intent(context, MainActivity::class.java).apply {
        action = "com.tolstykh.eatABurrita.widget.OPEN_MAP"
        putExtra(MainActivity.EXTRA_OPEN_MAP, true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val openShareIntent = Intent(context, MainActivity::class.java).apply {
        action = "com.tolstykh.eatABurrita.widget.OPEN_SHARE"
        putExtra(MainActivity.EXTRA_OPEN_SHARE, true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val openCameraIntent = Intent(context, MainActivity::class.java).apply {
        action = "com.tolstykh.eatABurrita.widget.OPEN_CAMERA"
        putExtra(MainActivity.EXTRA_OPEN_CAMERA, true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    GlanceBox(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = GlanceAlignment.Center,
    ) {
        GlanceBox(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(pillHeight)
                .background(ColorProvider(WidgetBgColor))
                .cornerRadius(100.dp),
            contentAlignment = GlanceAlignment.Center,
        ) {
            GlanceRow(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(pillHeight)
                    .padding(horizontal = 4.dp),
                verticalAlignment = GlanceAlignment.Vertical.CenterVertically,
            ) {
                // Left group: Eat! + Map
                GlanceBox(
                    modifier = GlanceModifier
                        .size(circleDp)
                        .cornerRadius(200.dp)
                        .background(ColorProvider(StatsOrange))
                        .clickable(actionRunCallback<LogBurritoStatsAction>()),
                    contentAlignment = GlanceAlignment.Center,
                ) {
                    GlanceText(
                        text = "Eat!",
                        style = GlanceTextStyle(
                            fontSize = 13.sp,
                            fontWeight = GlanceFontWeight.Bold,
                            color = ColorProvider(Color.White),
                        ),
                    )
                }
                GlanceBox(
                    modifier = GlanceModifier
                        .size(circleDp)
                        .cornerRadius(200.dp)
                        .background(ColorProvider(StatsBlue))
                        .clickable(actionStartActivity(openMapIntent)),
                    contentAlignment = GlanceAlignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_map),
                        contentDescription = "Map",
                        modifier = GlanceModifier.size(20.dp),
                    )
                }

                // Stats area
                GlanceBox(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .clickable(actionStartActivity(openAppIntent)),
                    contentAlignment = GlanceAlignment.Center,
                ) {
                    GlanceColumn(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = GlanceAlignment.Horizontal.CenterHorizontally,
                    ) {
                        GlanceText(
                            text = stat.label,
                            style = GlanceTextStyle(
                                fontSize = 10.sp,
                                fontWeight = GlanceFontWeight.Normal,
                                color = ColorProvider(StatLabelColor),
                            ),
                        )
                        GlanceText(
                            text = stat.value,
                            style = GlanceTextStyle(
                                fontSize = 14.sp,
                                fontWeight = GlanceFontWeight.Bold,
                                color = ColorProvider(Color.White),
                            ),
                        )
                    }
                }

                // Right group: Share + Camera
                GlanceBox(
                    modifier = GlanceModifier
                        .size(circleDp)
                        .cornerRadius(200.dp)
                        .background(ColorProvider(StatsPurple))
                        .clickable(actionStartActivity(openShareIntent)),
                    contentAlignment = GlanceAlignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_share),
                        contentDescription = "Share",
                        modifier = GlanceModifier.size(20.dp),
                    )
                }
                GlanceBox(
                    modifier = GlanceModifier
                        .size(circleDp)
                        .cornerRadius(200.dp)
                        .background(ColorProvider(StatsGreen))
                        .clickable(actionStartActivity(openCameraIntent)),
                    contentAlignment = GlanceAlignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_camera_alt),
                        contentDescription = "Camera",
                        modifier = GlanceModifier.size(20.dp),
                    )
                }
            }
        }
    }
}
