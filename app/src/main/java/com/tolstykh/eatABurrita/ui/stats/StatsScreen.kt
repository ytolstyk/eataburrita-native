package com.tolstykh.eatABurrita.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tolstykh.eatABurrita.ui.main.BurritoConsumptionChart

@Composable
fun StatsScreen(
    onBackPressed: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val stats by viewModel.statsData.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Spacer(Modifier.height(40.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Stats", style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(Modifier.height(16.dp))

            SummarySection(stats)

            Spacer(Modifier.height(28.dp))

            StatSectionTitle("Last 30 Days")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            BurritoConsumptionChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                dailyCounts = stats.dailyCounts,
                textMeasurer = rememberTextMeasurer(),
            )

            Spacer(Modifier.height(28.dp))

            StatSectionTitle("By Day of Week")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DayOfWeekChart(
                counts = stats.dayOfWeekCounts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            )

            Spacer(Modifier.height(28.dp))

            StatSectionTitle("Time of Day")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            HourOfDayChart(
                counts = stats.hourOfDayCounts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            )

            Spacer(Modifier.height(28.dp))

            StatSectionTitle("Monthly Trend")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            MonthlyChart(
                months = stats.monthlyCounts,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            )

            Spacer(Modifier.height(28.dp))

            StatSectionTitle("Top Spots")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            if (stats.topLocations.isEmpty()) {
                Text(
                    "No location data yet",
                    color = colorScheme.onSurface.copy(alpha = 0.45f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                TopLocationsChart(
                    locations = stats.topLocations,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = colorScheme.primary)
}

@Composable
private fun SummarySection(stats: StatsData) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatChip("Total Burritos", stats.totalCount.toString(), Modifier.weight(1f))
            StatChip("Avg / Week", "%.1f".format(stats.avgPerWeek), Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatChip("Current Streak", "${stats.currentStreak} days", Modifier.weight(1f))
            StatChip("Best Streak", "${stats.bestStreak} days", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
            )
        }
    }
}

private fun DrawScope.drawBarLabel(
    count: Int,
    barCenterX: Float,
    barTopY: Float,
    color: Color,
    textMeasurer: TextMeasurer,
) {
    val radius = 18.dp.toPx()
    val cy = (barTopY - radius - 4.dp.toPx()).coerceAtLeast(radius)
    drawCircle(color = color, radius = radius, center = Offset(barCenterX, cy))
    val layout = textMeasurer.measure(
        text = count.toString(),
        style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold),
    )
    drawText(
        layout,
        topLeft = Offset(barCenterX - layout.size.width / 2f, cy - layout.size.height / 2f),
    )
}

private val dayColors = listOf(
    Color(0xFFE53935),
    Color(0xFFFB8C00),
    Color(0xFFFFB300),
    Color(0xFF43A047),
    Color(0xFF00897B),
    Color(0xFF1E88E5),
    Color(0xFF8E24AA),
)

@Composable
private fun DayOfWeekChart(counts: List<Int>, modifier: Modifier) {
    val background = colorScheme.surfaceVariant
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(counts) {
                    detectTapGestures { offset ->
                        val barCount = counts.size
                        if (barCount == 0) return@detectTapGestures
                        val gap = 6.dp.toPx()
                        val barWidth = (size.width - gap * (barCount - 1)) / barCount
                        val idx = (offset.x / (barWidth + gap)).toInt().coerceIn(0, barCount - 1)
                        selectedIndex = if (selectedIndex == idx) null else idx
                    }
                }
        ) {
            val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
            val barCount = counts.size
            val gap = 6.dp.toPx()
            val barWidth = (size.width - gap * (barCount - 1)) / barCount

            counts.forEachIndexed { index, count ->
                val left = index * (barWidth + gap)
                val barHeight = (count.toFloat() / maxCount) * size.height

                drawRoundRect(
                    color = background,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(4.dp.toPx()),
                )
                if (count > 0) {
                    drawRoundRect(
                        color = dayColors[index],
                        topLeft = Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                }
            }

            selectedIndex?.let { idx ->
                val count = counts[idx]
                val left = idx * (barWidth + gap)
                val barHeight = (count.toFloat() / maxCount) * size.height
                drawBarLabel(count, left + barWidth / 2f, size.height - barHeight, dayColors[idx], textMeasurer)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            labels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = dayColors[index],
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private fun hourColor(hour: Int): Color = when (hour) {
    in 0..4 -> Color(0xFF283593)
    in 5..7 -> Color(0xFFF9A825)
    in 8..11 -> Color(0xFFFFCA28)
    in 12..14 -> Color(0xFFFF7043)
    in 15..17 -> Color(0xFFFF5722)
    in 18..20 -> Color(0xFF7E57C2)
    else -> Color(0xFF1A237E)
}

@Composable
private fun HourOfDayChart(counts: List<Int>, modifier: Modifier) {
    val background = colorScheme.surfaceVariant
    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(counts) {
                    detectTapGestures { offset ->
                        val barCount = counts.size
                        if (barCount == 0) return@detectTapGestures
                        val gap = 1.5.dp.toPx()
                        val barWidth = (size.width - gap * (barCount - 1)) / barCount
                        val idx = (offset.x / (barWidth + gap)).toInt().coerceIn(0, barCount - 1)
                        selectedIndex = if (selectedIndex == idx) null else idx
                    }
                }
        ) {
            val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
            val gap = 1.5.dp.toPx()
            val barWidth = (size.width - gap * (counts.size - 1)) / counts.size

            counts.forEachIndexed { index, count ->
                val left = index * (barWidth + gap)
                val barHeight = (count.toFloat() / maxCount) * size.height

                drawRoundRect(
                    color = background,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                )
                if (count > 0) {
                    drawRoundRect(
                        color = hourColor(index),
                        topLeft = Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                    )
                }
            }

            selectedIndex?.let { idx ->
                val count = counts[idx]
                val left = idx * (barWidth + gap)
                val barHeight = (count.toFloat() / maxCount) * size.height
                drawBarLabel(count, left + barWidth / 2f, size.height - barHeight, hourColor(idx), textMeasurer)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf("12am", "6am", "12pm", "6pm", "11pm").forEach { label ->
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }
        }
    }
}

@Composable
private fun MonthlyChart(months: List<Pair<String, Int>>, modifier: Modifier) {
    val primary = colorScheme.primary
    val background = colorScheme.surfaceVariant
    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(months) {
                    detectTapGestures { offset ->
                        val barCount = months.size
                        if (barCount == 0) return@detectTapGestures
                        val gap = 3.dp.toPx()
                        val barWidth = (size.width - gap * (barCount - 1)) / barCount
                        val idx = (offset.x / (barWidth + gap)).toInt().coerceIn(0, barCount - 1)
                        selectedIndex = if (selectedIndex == idx) null else idx
                    }
                }
        ) {
            val maxCount = months.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
            val barCount = months.size
            val gap = 3.dp.toPx()
            val barWidth = (size.width - gap * (barCount - 1)) / barCount

            months.forEachIndexed { index, (_, count) ->
                val left = index * (barWidth + gap)
                val barHeight = (count.toFloat() / maxCount) * size.height
                val alpha = 0.35f + 0.65f * index / (barCount - 1).coerceAtLeast(1)

                drawRoundRect(
                    color = background,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                )
                if (count > 0) {
                    drawRoundRect(
                        color = primary.copy(alpha = alpha),
                        topLeft = Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3.dp.toPx()),
                    )
                }
            }

            selectedIndex?.let { idx ->
                val (_, count) = months[idx]
                val left = idx * (barWidth + gap)
                val barHeight = (count.toFloat() / maxCount) * size.height
                val alpha = 0.35f + 0.65f * idx / (barCount - 1).coerceAtLeast(1)
                drawBarLabel(count, left + barWidth / 2f, size.height - barHeight, primary.copy(alpha = alpha), textMeasurer)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            months.forEach { (label, _) ->
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TopLocationsChart(locations: List<Pair<String, Int>>, modifier: Modifier) {
    val maxCount = locations.maxOf { it.second }.coerceAtLeast(1)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        locations.forEachIndexed { index, (name, count) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = name,
                    modifier = Modifier.width(110.dp),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f).height(22.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = (count.toFloat() / maxCount) * size.width
                        drawRoundRect(
                            color = dayColors[index % dayColors.size],
                            topLeft = Offset(0f, 0f),
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(4.dp.toPx()),
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}
