package com.tolstykh.eatABurrita.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolstykh.eatABurrita.data.BurritoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val dao: BurritoDao,
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli()
    private val twelveMonthsAgo = YearMonth.now(zone).minusMonths(12)
        .atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()

    // Group 1: summary — total count, distinct days (for streaks + avg/week)
    private val summaryFlow = combine(
        dao.getCount(),
        dao.getDistinctDays(),
    ) { count, dayStrings ->
        val today = LocalDate.now(zone)
        val days = dayStrings.map { LocalDate.parse(it.day) }
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

        Triple(count, Triple(currentStreak, bestStreak, avgPerWeek), Unit)
    }

    // Group 2: chart data — daily, day-of-week, hour, monthly, locations
    private val chartFlow = combine(
        dao.getEntriesSince(thirtyDaysAgo),
        dao.getDayOfWeekCounts(),
        dao.getHourOfDayCounts(),
        dao.getMonthlyCounts(twelveMonthsAgo),
        dao.getTopLocations(),
    ) { recentEntries, dowRows, hourRows, monthRows, locationRows ->
        val today = LocalDate.now(zone)

        // 30-day daily counts — bounded query, cheap to process
        val countByDay = recentEntries
            .map { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
            .groupingBy { it }.eachCount()
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
            val key = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            Pair(month.format(DateTimeFormatter.ofPattern("MMM")), monthlyMap[key] ?: 0)
        }

        // Top locations — already limited to 5 by SQL
        val topLocations = locationRows.map { Pair(it.locationName, it.cnt) }

        listOf(dailyCounts, dowArr.toList(), hourArr.toList(), monthlyCounts, topLocations)
    }

    @Suppress("UNCHECKED_CAST")
    val statsData: StateFlow<StatsData> = combine(summaryFlow, chartFlow) { summary, charts ->
        val (count, streakData, _) = summary
        val (currentStreak, bestStreak, avgPerWeek) = streakData

        StatsData(
            totalCount = count,
            dailyCounts = charts[0] as List<Int>,
            dayOfWeekCounts = charts[1] as List<Int>,
            hourOfDayCounts = charts[2] as List<Int>,
            monthlyCounts = charts[3] as List<Pair<String, Int>>,
            topLocations = charts[4] as List<Pair<String, Int>>,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            avgPerWeek = avgPerWeek,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StatsData(),
    )
}
