package com.tolstykh.eatABurrita.ui.main

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolstykh.eatABurrita.appDataStore
import com.tolstykh.eatABurrita.ui.main.TimeScreenViewModel.TimeScreenUIState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

val BURRITO_COUNTER_KEY: Preferences.Key<Int> = intPreferencesKey("burrito_counter")
val TIMESTAMP_KEY = longPreferencesKey("timestamp")

data class TimeScreenData(
    val burritoCount: Int,
    val lastTimestamp: Long,
)

@HiltViewModel
class TimeScreenViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {
    private val dataStore = context.appDataStore

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _timeScreenState =
        dataStore.data.flatMapLatest { data ->
            flowOf(TimeScreenData(data[BURRITO_COUNTER_KEY] ?: 0, data[TIMESTAMP_KEY] ?: 0L))
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TimeScreenData(0, 0L)
            )

    val timeScreenState: StateFlow<TimeScreenUIState> =
        _timeScreenState
            .map<TimeScreenData, TimeScreenUIState>(::Success)
            .catch { emit(TimeScreenUIState.Error(it)) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TimeScreenUIState.Loading
            )

    fun addBurrito() {
        viewModelScope.launch {
            dataStore.edit { store ->
                val currentCounterValue = store[BURRITO_COUNTER_KEY] ?: 0
                store[BURRITO_COUNTER_KEY] = currentCounterValue + 1
                store[TIMESTAMP_KEY] = Instant.now().toEpochMilli()
            }
        }
    }

    sealed interface TimeScreenUIState {
        data object Loading : TimeScreenUIState
        data class Error(val throwable: Throwable) : TimeScreenUIState
        data class Success(val data: TimeScreenData) : TimeScreenUIState
    }
}
