package com.tolstykh.eatABurrita.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.runtime.snapshotFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.first
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tolstykh.eatABurrita.ui.main.BurritoConsumptionChart

@Composable
fun StatsScreen(
    onBackPressed: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val stats by viewModel.statsData.collectAsStateWithLifecycle()
    val newlyUnlocked by viewModel.newlyUnlockedAchievements.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    if (newlyUnlocked.isNotEmpty()) {
        AchievementUnlockedDialog(
            achievements = newlyUnlocked,
            onDismiss = { viewModel.markAchievementsSeen() },
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Fixed header
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text("Stats", style = MaterialTheme.typography.headlineMedium)
                    }

                }
                Spacer(Modifier.height(8.dp))
            }

            // Sticky tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Stats") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Achievements") },
                )
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                if (selectedTab == 0) {
                    if (stats.totalCount == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No stats yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = colorScheme.onSurface,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Start logging burritos to see your stats here.",
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurface.copy(alpha = 0.55f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        SummarySection(stats)

                        if (stats.totalCalories > 0) {
                            Spacer(Modifier.height(16.dp))
                            CalorieBankSection(stats.totalCalories)
                        }

                        Spacer(Modifier.height(28.dp))

                        StatSectionTitle("Yearly Heatmap")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        StreakHeatmapChart(
                            heatmapData = stats.heatmapData,
                            startDate = stats.heatmapStartDate,
                            modifier = Modifier.fillMaxWidth(),
                        )

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
                } else {
                    AchievementsSection(achievements = stats.achievements)
                    Spacer(Modifier.height(32.dp))
                }
            }
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

@Composable
private fun CalorieBankSection(calories: Int) {
    val teslaDays = calories / 15_767
    val netflixHours = (calories / 38.1).toInt()
    val stairFlights = (calories * 2.494).toInt()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.primaryContainer,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Calorie Bank",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
            Text(
                "%,d kcal".format(calories),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(10.dp))
            CalorieEquivalentRow("⚡", "= enough to power a Tesla for $teslaDays days")
            Spacer(Modifier.height(4.dp))
            CalorieEquivalentRow("📺", "= %,d hours of Netflix".format(netflixHours))
            Spacer(Modifier.height(4.dp))
            CalorieEquivalentRow("🪜", "= %,d flights of stairs".format(stairFlights))
        }
    }
}

@Composable
private fun CalorieEquivalentRow(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
        )
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

private val heatmapOrangeScale = listOf(
    Color(0xFFFFE0B2),
    Color(0xFFFFB74D),
    Color(0xFFFF9800),
    Color(0xFFE65100),
)

private val selectedDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

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
private fun StreakHeatmapChart(
    heatmapData: Map<LocalDate, Int>,
    startDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    val cellSize = 11.dp
    val gap = 2.dp
    val step = cellSize + gap
    val totalWeeks = 53
    val monthRowHeight = 16.dp
    val dayLabelWidth = 20.dp

    val today = remember { LocalDate.now() }

    // Pre-compute the full date grid once — avoids 371 LocalDate allocations per draw frame
    val cellDates = remember(startDate) {
        Array(totalWeeks) { col ->
            Array(7) { row -> startDate.plusWeeks(col.toLong()).plusDays(row.toLong()) }
        }
    }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val scrollState = rememberScrollState()

    // Scroll to today's column exactly once after layout settles
    LaunchedEffect(Unit) {
        snapshotFlow { scrollState.maxValue }
            .first { it > 0 }
            .let { scrollState.scrollTo(it) }
    }

    val emptyColor = colorScheme.surfaceVariant
    val dayLabelColor = colorScheme.onSurface.copy(alpha = 0.45f)
    val monthLabelColor = colorScheme.onSurface.copy(alpha = 0.55f)
    val primaryColor = colorScheme.primary
    val textMeasurer = rememberTextMeasurer()

    // Pre-compute month label layouts once — avoids textMeasurer.measure in the draw loop
    val monthLayouts = remember(startDate, textMeasurer) {
        val labelStyle = TextStyle(color = Color.Unspecified, fontSize = 9.sp)
        buildList {
            var lastCol = -4
            for (col in 0 until totalWeeks) {
                val colDate = startDate.plusWeeks(col.toLong())
                val prevDate = if (col > 0) startDate.plusWeeks((col - 1).toLong()) else null
                if ((prevDate == null || colDate.monthValue != prevDate.monthValue) && col - lastCol >= 3) {
                    val label = colDate.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())
                    add(Triple(col, textMeasurer.measure(text = label, style = labelStyle), colDate))
                    lastCol = col
                }
            }
        }
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top) {
            // Fixed day-of-week labels (S M T W T F S)
            Column(modifier = Modifier.width(dayLabelWidth).padding(top = monthRowHeight)) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                    Box(
                        modifier = Modifier.size(width = dayLabelWidth, height = step),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, fontSize = 8.sp, color = dayLabelColor)
                    }
                }
            }

            // Scrollable month labels + grid
            Box(modifier = Modifier.horizontalScroll(scrollState)) {
                Canvas(
                    modifier = Modifier
                        .width(step * totalWeeks - gap)
                        .height(monthRowHeight + step * 7 - gap)
                        .pointerInput(cellDates, today) {
                            detectTapGestures { offset ->
                                val stepPx = step.toPx()
                                val monthHeightPx = monthRowHeight.toPx()
                                if (offset.y < monthHeightPx) return@detectTapGestures
                                val col = (offset.x / stepPx).toInt().coerceIn(0, totalWeeks - 1)
                                val row = ((offset.y - monthHeightPx) / stepPx).toInt().coerceIn(0, 6)
                                val tapped = cellDates[col][row]
                                if (!tapped.isAfter(today)) {
                                    selectedDate = if (selectedDate == tapped) null else tapped
                                }
                            }
                        },
                ) {
                    val cellSizePx = cellSize.toPx()
                    val stepPx = step.toPx()
                    val cornerPx = CornerRadius(2.dp.toPx())
                    val gridTopPx = monthRowHeight.toPx()

                    // Draw pre-computed month labels
                    monthLayouts.forEach { (col, layout, _) ->
                        drawText(
                            layout,
                            color = monthLabelColor,
                            topLeft = Offset(col * stepPx, (gridTopPx - layout.size.height) / 2f),
                        )
                    }

                    // Grid cells — indexes into pre-computed date array, no allocations
                    for (col in 0 until totalWeeks) {
                        for (row in 0..6) {
                            val date = cellDates[col][row]
                            if (date.isAfter(today)) continue
                            val count = heatmapData[date] ?: 0
                            val cellColor = when {
                                date == selectedDate -> primaryColor
                                count == 0 -> emptyColor
                                count == 1 -> heatmapOrangeScale[0]
                                count <= 3 -> heatmapOrangeScale[1]
                                count <= 6 -> heatmapOrangeScale[2]
                                else -> heatmapOrangeScale[3]
                            }
                            drawRoundRect(
                                color = cellColor,
                                topLeft = Offset(col * stepPx, gridTopPx + row * stepPx),
                                size = Size(cellSizePx, cellSizePx),
                                cornerRadius = cornerPx,
                            )
                        }
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = dayLabelWidth),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Less", fontSize = 9.sp, color = colorScheme.onSurface.copy(alpha = 0.5f))
            Spacer(Modifier.width(4.dp))
            val legendColors = remember(emptyColor) { listOf(emptyColor) + heatmapOrangeScale }
            legendColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .size(9.dp)
                        .background(color = color, shape = RoundedCornerShape(2.dp)),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text("More", fontSize = 9.sp, color = colorScheme.onSurface.copy(alpha = 0.5f))
        }

        // Selected date info
        selectedDate?.let { date ->
            val count = heatmapData[date] ?: 0
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = date.format(selectedDateFormatter),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = when (count) {
                            0 -> "No burritos"
                            1 -> "1 burrito"
                            else -> "$count burritos"
                        },
                        fontSize = 13.sp,
                        fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (count > 0) primaryColor else colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
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
