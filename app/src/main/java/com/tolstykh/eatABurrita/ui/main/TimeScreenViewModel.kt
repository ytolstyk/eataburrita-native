package com.tolstykh.eatABurrita.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolstykh.eatABurrita.data.BurritoDao
import com.tolstykh.eatABurrita.data.BurritoEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class TimeScreenData(
    val burritoCount: Int,
    val lastTimestamp: Long,
    val dailyCounts: List<Int>,
)

@HiltViewModel
class TimeScreenViewModel @Inject constructor(
    private val dao: BurritoDao,
) : ViewModel() {

    private val thirtyDaysAgo: Long = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli()

    val timeScreenState: StateFlow<TimeScreenUIState> =
        combine(
            dao.getCount(),
            dao.getLatestTimestamp(),
            dao.getEntriesSince(thirtyDaysAgo),
        ) { count, lastTs, entries ->
            TimeScreenData(count, lastTs ?: 0L, buildDailyCounts(entries))
        }
            .map<TimeScreenData, TimeScreenUIState>(TimeScreenUIState::Success)
            .catch { emit(TimeScreenUIState.Error(it)) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TimeScreenUIState.Loading,
            )

    fun addBurrito() {
        viewModelScope.launch {
            dao.insert(BurritoEntry(timestamp = Instant.now().toEpochMilli()))
        }
    }

    private fun buildDailyCounts(entries: List<BurritoEntry>): List<Int> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val countByDay = entries
            .map { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
            .groupingBy { it }
            .eachCount()
        return (29 downTo 0).map { daysAgo ->
            countByDay[today.minusDays(daysAgo.toLong())] ?: 0
        }
    }

    sealed interface TimeScreenUIState {
        data object Loading : TimeScreenUIState
        data class Error(val throwable: Throwable) : TimeScreenUIState
        data class Success(val data: TimeScreenData) : TimeScreenUIState
    }
}
