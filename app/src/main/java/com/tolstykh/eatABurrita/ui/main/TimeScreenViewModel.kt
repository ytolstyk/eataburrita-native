package com.tolstykh.eatABurrita.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.tolstykh.eatABurrita.data.AppPreferencesRepository
import com.tolstykh.eatABurrita.data.BurritoDao
import com.tolstykh.eatABurrita.data.BurritoEntry
import com.tolstykh.eatABurrita.location.GetLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class TimeScreenData(
    val burritoCount: Int,
    val lastTimestamp: Long,
    val dailyCounts: List<Int>,
    val favoritePlaceName: String? = null,
)

@HiltViewModel
class TimeScreenViewModel @Inject constructor(
    private val dao: BurritoDao,
    private val appPrefs: AppPreferencesRepository,
    private val getLocationUseCase: GetLocationUseCase,
) : ViewModel() {

    private val thirtyDaysAgo: Long = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli()

    val timeScreenState: StateFlow<TimeScreenUIState> =
        combine(
            dao.getCount(),
            dao.getLatestTimestamp(),
            dao.getEntriesSince(thirtyDaysAgo),
            dao.getEntriesWithLocation(),
        ) { count, lastTs, entries, locationEntries ->
            TimeScreenData(
                burritoCount = count,
                lastTimestamp = lastTs ?: 0L,
                dailyCounts = buildDailyCounts(entries),
                favoritePlaceName = buildFavoritePlace(locationEntries),
            )
        }
            .map<TimeScreenData, TimeScreenUIState>(TimeScreenUIState::Success)
            .catch { emit(TimeScreenUIState.Error(it)) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                TimeScreenUIState.Loading,
            )

    val showLocationModal: StateFlow<Boolean> = appPrefs.showLocationModal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _locationPickerOpen = MutableStateFlow(false)
    val locationPickerOpen: StateFlow<Boolean> = _locationPickerOpen.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentUserLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val _dayLocationModal = MutableStateFlow<DayLocationData?>(null)
    val dayLocationModal: StateFlow<DayLocationData?> = _dayLocationModal.asStateFlow()

    init {
        viewModelScope.launch {
            getLocationUseCase().collect { latLng ->
                _currentLocation.value = latLng
            }
        }
    }

    fun requestAddBurrito() {
        viewModelScope.launch {
            if (appPrefs.showLocationModal.first()) {
                _locationPickerOpen.value = true
            } else {
                dao.insert(BurritoEntry(timestamp = Instant.now().toEpochMilli()))
            }
        }
    }

    fun confirmAddBurrito(name: String?, lat: Double?, lng: Double?) {
        _locationPickerOpen.value = false
        viewModelScope.launch {
            dao.insert(
                BurritoEntry(
                    timestamp = Instant.now().toEpochMilli(),
                    locationName = name,
                    locationLat = lat,
                    locationLong = lng,
                )
            )
        }
    }

    fun cancelAddBurrito() {
        _locationPickerOpen.value = false
        viewModelScope.launch {
            dao.insert(BurritoEntry(timestamp = Instant.now().toEpochMilli()))
        }
    }

    fun disableLocationModal() {
        viewModelScope.launch { appPrefs.setShowLocationModal(false) }
    }

    fun onChartDayClicked(dayIndex: Int) {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val clickedDate = LocalDate.now(zone).minusDays((29 - dayIndex).toLong())
            val dayStart = clickedDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = clickedDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val entries = dao.getEntriesWithLocationForDay(dayStart, dayEnd)
            if (entries.isNotEmpty()) {
                _dayLocationModal.value = DayLocationData(
                    dateLabel = clickedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    entries = entries,
                )
            }
        }
    }

    fun dismissDayLocationModal() {
        _dayLocationModal.value = null
    }

    private fun buildFavoritePlace(entries: List<BurritoEntry>): String? =
        entries
            .mapNotNull { it.locationName }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

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

    data class DayLocationData(
        val dateLabel: String,
        val entries: List<BurritoEntry>,
    )

    sealed interface TimeScreenUIState {
        data object Loading : TimeScreenUIState
        data class Error(val throwable: Throwable) : TimeScreenUIState
        data class Success(val data: TimeScreenData) : TimeScreenUIState
    }
}
