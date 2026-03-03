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
import javax.inject.Inject

data class TimeScreenData(
    val burritoCount: Int,
    val lastTimestamp: Long,
)

@HiltViewModel
class TimeScreenViewModel @Inject constructor(
    private val dao: BurritoDao,
) : ViewModel() {

    val timeScreenState: StateFlow<TimeScreenUIState> =
        dao.getCount()
            .combine(dao.getLatestTimestamp()) { count, lastTs ->
                TimeScreenData(count, lastTs ?: 0L)
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

    sealed interface TimeScreenUIState {
        data object Loading : TimeScreenUIState
        data class Error(val throwable: Throwable) : TimeScreenUIState
        data class Success(val data: TimeScreenData) : TimeScreenUIState
    }
}
