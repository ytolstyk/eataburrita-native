package com.tolstykh.eatABurrita.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolstykh.eatABurrita.data.AppPreferencesRepository
import com.tolstykh.eatABurrita.data.BurritoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class StatsData(
    val totalCount: Int = 0,
    val dailyCounts: List<Int> = List(30) { 0 },
    val dayOfWeekCounts: List<Int> = List(7) { 0 },
    val hourOfDayCounts: List<Int> = List(24) { 0 },
    val monthlyCounts: List<Pair<String, Int>> = emptyList(),
    val topLocations: List<Pair<String, Int>> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val avgPerWeek: Float = 0f,
    val totalCalories: Int = 0,
    val distinctLocationCount: Int = 0,
    val achievements: List<Achievement> = emptyList(),
    val heatmapData: Map<LocalDate, Int> = emptyMap(),
    val heatmapStartDate: LocalDate = LocalDate.MIN,
)

private data class SummaryBundle(
    val totalCount: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val avgPerWeek: Float,
    val totalCalories: Int,
    val distinctLocationCount: Int,
)

private data class ChartBundle(
    val dailyCounts: List<Int>,
    val dayOfWeekCounts: List<Int>,
    val hourOfDayCounts: List<Int>,
    val monthlyCounts: List<Pair<String, Int>>,
    val topLocations: List<Pair<String, Int>>,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val dao: BurritoDao,
    private val prefs: AppPreferencesRepository,
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli()
    private val twelveMonthsAgo = YearMonth.now(zone).minusMonths(12)
        .atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    private val heatmapStartDate = run {
        val today = LocalDate.now(zone)
        val thisSunday = today.minusDays((today.dayOfWeek.value % 7).toLong())
        thisSunday.minusWeeks(52)
    }
    private val heatmapStartMs = heatmapStartDate.atStartOfDay(zone).toInstant().toEpochMilli()

    private val monthKeyFmt = DateTimeFormatter.ofPattern("yyyy-MM")
    private val monthLabelFmt = DateTimeFormatter.ofPattern("MMM")

    // Group 1: summary — total count, distinct days (for streaks + avg/week), total calories, distinct locations
    private val summaryFlow = combine(
        dao.getCount(),
        dao.getDistinctDays(),
        dao.getTotalCalories(),
        dao.getDistinctLocationCount(),
    ) { count, dayStrings, totalCalories, distinctLocationCount ->
        val today = LocalDate.now(zone)
        val days = dayStrings.mapNotNull { runCatching { LocalDate.parse(it.day) }.getOrNull() }
        val daySet = days.toHashSet()

        var currentStreak = 0
        var d = today
        while (d in daySet) { currentStreak++; d = d.minusDays(1) }

        var bestStreak = 0; var streak = 0; var lastDay: LocalDate? = null
        days.forEach { day ->
            streak = if (lastDay == null || day == lastDay!!.plusDays(1)) streak + 1 else 1
            bestStreak = maxOf(bestStreak, streak)
            lastDay = day
        }

        val avgPerWeek = if (days.isEmpty()) 0f else {
            val weeksSince = ChronoUnit.WEEKS.between(days.first(), today).coerceAtLeast(1)
            count.toFloat() / weeksSince
        }

        SummaryBundle(
            totalCount = count,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            avgPerWeek = avgPerWeek,
            totalCalories = totalCalories,
            distinctLocationCount = distinctLocationCount,
        )
    }

    // Group 2: chart data — daily, day-of-week, hour, monthly, locations
    private val chartFlow = combine(
        dao.getDailyCountsSince(thirtyDaysAgo),
        dao.getDayOfWeekCounts(),
        dao.getHourOfDayCounts(),
        dao.getMonthlyCounts(twelveMonthsAgo),
        dao.getTopLocations(),
    ) { recentDayCounts, dowRows, hourRows, monthRows, locationRows ->
        val today = LocalDate.now(zone)

        // 30-day daily counts — aggregate from SQL, no full-row deserialization
        val countByDay = recentDayCounts.mapNotNull { row ->
            runCatching { LocalDate.parse(row.day) }.getOrNull()?.let { it to row.cnt }
        }.toMap()
        val dailyCounts = (29 downTo 0).map { daysAgo ->
            countByDay[today.minusDays(daysAgo.toLong())] ?: 0
        }

        // Day of week: SQLite %w gives 0=Sun..6=Sat → convert to 0=Mon..6=Sun
        val dowArr = IntArray(7)
        dowRows.forEach { (dow, cnt) -> dowArr[(dow + 6) % 7] = cnt }

        // Hour of day
        val hourArr = IntArray(24)
        hourRows.forEach { (hour, cnt) -> hourArr[hour] = cnt }

        // Monthly — fill all 12 months (SQL only returns months with data)
        val monthlyMap = monthRows.associate { it.month to it.cnt }
        val monthlyCounts = (11 downTo 0).map { monthsAgo ->
            val month = YearMonth.now(zone).minusMonths(monthsAgo.toLong())
            Pair(month.format(monthLabelFmt), monthlyMap[month.format(monthKeyFmt)] ?: 0)
        }

        // Top locations — already limited to 5 by SQL
        val topLocations = locationRows.map { Pair(it.locationName, it.cnt) }

        ChartBundle(
            dailyCounts = dailyCounts,
            dayOfWeekCounts = dowArr.toList(),
            hourOfDayCounts = hourArr.toList(),
            monthlyCounts = monthlyCounts,
            topLocations = topLocations,
        )
    }

    // Group 3: heatmap — kept separate from chartFlow because chartFlow already uses the 5-arg combine limit
    private val heatmapFlow = dao.getDailyCountsSince(heatmapStartMs).map { rows ->
        rows.mapNotNull { row ->
            runCatching { LocalDate.parse(row.day) }.getOrNull()?.let { it to row.cnt }
        }.toMap()
    }

    val statsData: StateFlow<StatsData> = combine(summaryFlow, chartFlow, heatmapFlow) { summary, charts, heatmap ->
        val base = StatsData(
            totalCount = summary.totalCount,
            dailyCounts = charts.dailyCounts,
            dayOfWeekCounts = charts.dayOfWeekCounts,
            hourOfDayCounts = charts.hourOfDayCounts,
            monthlyCounts = charts.monthlyCounts,
            topLocations = charts.topLocations,
            currentStreak = summary.currentStreak,
            bestStreak = summary.bestStreak,
            avgPerWeek = summary.avgPerWeek,
            totalCalories = summary.totalCalories,
            distinctLocationCount = summary.distinctLocationCount,
            heatmapData = heatmap,
            heatmapStartDate = heatmapStartDate,
        )
        base.copy(achievements = computeAchievements(base, summary.distinctLocationCount))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StatsData(),
    )

    val newlyUnlockedAchievements: StateFlow<List<Achievement>> = combine(
        statsData,
        prefs.unlockedAchievements,
    ) { stats, seenIds ->
        stats.achievements.filter { it.isUnlocked && it.id !in seenIds }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList(),
    )

    fun markAchievementsSeen() {
        viewModelScope.launch {
            val ids = statsData.value.achievements.filter { it.isUnlocked }.map { it.id }.toSet()
            prefs.markAchievementsUnlocked(ids)
        }
    }
}
