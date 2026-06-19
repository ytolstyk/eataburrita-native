package com.tolstykh.eatABurrita.ui.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.tolstykh.eatABurrita.helpers.copyToPhotoLog
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.tolstykh.eatABurrita.classifier.BurritoClassifier
import com.tolstykh.eatABurrita.classifier.ClassificationOutcome
import com.tolstykh.eatABurrita.data.AppPreferencesRepository
import com.tolstykh.eatABurrita.data.BurritoDao
import com.tolstykh.eatABurrita.data.BurritoEntry
import com.tolstykh.eatABurrita.location.GetLocationUseCase
import com.tolstykh.eatABurrita.notification.BurritoNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
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

private const val ON_THIS_DAY_WINDOW_DAYS = 3L

data class WeekEntryUi(
    val timestamp: Long,
    val locationName: String?,
)

data class OnThisDayData(
    val yearAgoDate: LocalDate,
    val dayCount: Int,
    val topLocation: String?,
    val weekEntries: List<WeekEntryUi>,
    val weekCount: Int,
    val weekDays: List<LocalDate>,
) {
    val summary: String
        get() {
            val burritoText = if (dayCount == 1) "1 burrito" else "$dayCount burritos"
            return if (topLocation != null) "You had $burritoText at $topLocation 🌯"
            else "You had $burritoText 🌯"
        }
}

data class TimeScreenData(
    val burritoCount: Int,
    val lastTimestamp: Long,
    val dailyCounts: List<Int>,
    val favoritePlaceName: String? = null,
    val favoritePlaceLat: Double? = null,
    val favoritePlaceLng: Double? = null,
    val lastPlaceName: String? = null,
    val lastPlaceLat: Double? = null,
    val lastPlaceLng: Double? = null,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val hourOfDayCounts: List<Int> = List(24) { 0 },
    val dayOfWeekCounts: List<Int> = List(7) { 0 },
    val yearCount: Int = 0,
)

@HiltViewModel
class TimeScreenViewModel @Inject constructor(
    private val dao: BurritoDao,
    private val appPrefs: AppPreferencesRepository,
    private val getLocationUseCase: GetLocationUseCase,
    private val burritoClassifier: BurritoClassifier,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val thirtyDaysAgo: Long = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli()
    private val yearStart: Long = LocalDate.now(zone).withDayOfYear(1).atStartOfDay(zone).toInstant().toEpochMilli()

    private val _today = MutableStateFlow(LocalDate.now(zone))

    fun onTimerTick() {
        val today = LocalDate.now(zone)
        if (_today.value != today) _today.value = today
    }

    // Group A: streak computation + year count + total count
    private val streakFlow = combine(
        dao.getDistinctDays(),
        dao.getCount(),
        dao.getEntriesSince(yearStart),
        _today,
    ) { dayRows, count, yearEntries, today ->
        val days = dayRows.map { LocalDate.parse(it.day) }
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

        listOf(currentStreak, bestStreak, count, yearEntries.size)
    }

    // Group B: time-of-day and day-of-week patterns for personality
    private val patternFlow = combine(
        dao.getHourOfDayCounts(),
        dao.getDayOfWeekCounts(),
    ) { hourRows, dowRows ->
        val hourArr = IntArray(24)
        hourRows.forEach { (hour, cnt) -> hourArr[hour] = cnt }
        val dowArr = IntArray(7)
        dowRows.forEach { (dow, cnt) -> dowArr[(dow + 6) % 7] = cnt }
        Pair(hourArr.toList(), dowArr.toList())
    }

    val timeScreenState: StateFlow<TimeScreenUIState> =
        combine(
            streakFlow,
            patternFlow,
            dao.getLatestTimestamp(),
            dao.getEntriesSince(thirtyDaysAgo),
            dao.getEntriesWithLocation(),
        ) { streakData, (hourOfDayCounts, dayOfWeekCounts), lastTs, recentEntries, locationEntries ->
            val (currentStreak, bestStreak, count, yearCount) = streakData
            val (favName, favLat, favLng) = buildFavoritePlace(locationEntries)
            val lastEntry = locationEntries.firstOrNull()
            TimeScreenData(
                burritoCount = count,
                lastTimestamp = lastTs ?: 0L,
                dailyCounts = buildDailyCounts(recentEntries, _today.value),
                favoritePlaceName = favName,
                favoritePlaceLat = favLat,
                favoritePlaceLng = favLng,
                lastPlaceName = lastEntry?.locationName,
                lastPlaceLat = lastEntry?.locationLat,
                lastPlaceLng = lastEntry?.locationLong,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                hourOfDayCounts = hourOfDayCounts,
                dayOfWeekCounts = dayOfWeekCounts,
                yearCount = yearCount,
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

    val notificationPermissionAsked: StateFlow<Boolean> = appPrefs.notificationPermissionAsked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun markNotificationPermissionAsked() {
        viewModelScope.launch { appPrefs.setNotificationPermissionAsked() }
    }

    private val _locationPickerOpen = MutableStateFlow(false)
    val locationPickerOpen: StateFlow<Boolean> = _locationPickerOpen.asStateFlow()

    private val _sizePickerOpen = MutableStateFlow(false)
    val sizePickerOpen: StateFlow<Boolean> = _sizePickerOpen.asStateFlow()

    private val _photoPickerOpen = MutableStateFlow(false)
    val photoPickerOpen: StateFlow<Boolean> = _photoPickerOpen.asStateFlow()

    private var _pendingCalories: Int = 0
    private var _pendingLocationName: String? = null
    private var _pendingLocationLat: Double? = null
    private var _pendingLocationLng: Double? = null

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentUserLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val _dayLocationModal = MutableStateFlow<DayLocationData?>(null)
    val dayLocationModal: StateFlow<DayLocationData?> = _dayLocationModal.asStateFlow()

    private val _onThisDayData = MutableStateFlow<OnThisDayData?>(null)
    val onThisDayData: StateFlow<OnThisDayData?> = _onThisDayData.asStateFlow()

    fun dismissOnThisDay() {
        _onThisDayData.value = null
    }

    private val _onThisDayWeekVisible = MutableStateFlow(false)
    val onThisDayWeekVisible: StateFlow<Boolean> = _onThisDayWeekVisible.asStateFlow()

    fun openOnThisDayWeek() { _onThisDayWeekVisible.value = true }
    fun dismissOnThisDayWeek() { _onThisDayWeekVisible.value = false }

    private var locationJob: Job? = null

    init {
        refreshLocation()
        loadOnThisDay()
    }

    private fun loadOnThisDay() {
        viewModelScope.launch {
            val today = LocalDate.now(zone)
            val yearAgo = today.minusYears(1)
            val weekStart = yearAgo.minusDays(ON_THIS_DAY_WINDOW_DAYS)
            val weekStartMs = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
            val weekEndMs = yearAgo.plusDays(ON_THIS_DAY_WINDOW_DAYS + 1).atStartOfDay(zone).toInstant().toEpochMilli()

            val allWeekEntries = dao.getEntriesInRange(weekStartMs, weekEndMs)
            val dayStartMs = yearAgo.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEndMs = yearAgo.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEntries = allWeekEntries.filter { it.timestamp in dayStartMs until dayEndMs }
            if (dayEntries.isEmpty()) return@launch

            val topLocation = dayEntries.mostFrequentLocationName()
            val weekDayCount = (ON_THIS_DAY_WINDOW_DAYS * 2 + 1).toInt()
            val weekDays = (0 until weekDayCount).map { weekStart.plusDays(it.toLong()) }

            _onThisDayData.value = OnThisDayData(
                yearAgoDate = yearAgo,
                dayCount = dayEntries.size,
                topLocation = topLocation,
                weekEntries = allWeekEntries.map { WeekEntryUi(it.timestamp, it.locationName) },
                weekCount = allWeekEntries.size,
                weekDays = weekDays,
            )
        }
    }

    fun refreshLocation() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
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
                _pendingLocationName = null
                _pendingLocationLat = null
                _pendingLocationLng = null
                _sizePickerOpen.value = true
            }
        }
    }

    fun confirmAddBurrito(name: String?, lat: Double?, lng: Double?) {
        _locationPickerOpen.value = false
        _pendingLocationName = name
        _pendingLocationLat = lat
        _pendingLocationLng = lng
        _sizePickerOpen.value = true
    }

    fun cancelAddBurrito() {
        _locationPickerOpen.value = false
        _pendingLocationName = null
        _pendingLocationLat = null
        _pendingLocationLng = null
        _sizePickerOpen.value = true
    }

    fun onSizeConfirmed(calories: Int) {
        _sizePickerOpen.value = false
        _pendingCalories = calories
        _photoPickerOpen.value = true
    }

    fun onSizeSkipped() {
        _sizePickerOpen.value = false
        _pendingCalories = 0
        _photoPickerOpen.value = true
    }

    fun onPhotoConfirmed(photoUri: Uri?) {
        _photoPickerOpen.value = false
        val calories = _pendingCalories
        viewModelScope.launch {
            val savedPath = photoUri?.let { copyToPhotoLog(context, it) }
            dao.insert(
                BurritoEntry(
                    timestamp = Instant.now().toEpochMilli(),
                    locationName = _pendingLocationName,
                    locationLat = _pendingLocationLat,
                    locationLong = _pendingLocationLng,
                    calories = if (calories > 0) calories else null,
                    photoPath = savedPath,
                )
            )
            appPrefs.resetNotificationSentFlags()
            computeAndTriggerCelebration()
        }
    }

    fun onPhotoSkipped() {
        _photoPickerOpen.value = false
        val calories = _pendingCalories
        viewModelScope.launch {
            dao.insert(
                BurritoEntry(
                    timestamp = Instant.now().toEpochMilli(),
                    locationName = _pendingLocationName,
                    locationLat = _pendingLocationLat,
                    locationLong = _pendingLocationLng,
                    calories = if (calories > 0) calories else null,
                )
            )
            appPrefs.resetNotificationSentFlags()
            computeAndTriggerCelebration()
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
            val totalCount = dao.getCountForDay(dayStart, dayEnd)
            if (totalCount > 0) {
                _dayLocationModal.value = DayLocationData(
                    dateLabel = clickedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    entries = entries.take(10),
                    totalCount = totalCount,
                )
            }
        }
    }

    fun dismissDayLocationModal() {
        _dayLocationModal.value = null
    }

    sealed interface CelebrationState {
        data object None : CelebrationState
        data object EmojiRain : CelebrationState
        data class StreakMilestone(val days: Int) : CelebrationState
    }

    private val _celebrationState = MutableStateFlow<CelebrationState>(CelebrationState.None)
    val celebrationState: StateFlow<CelebrationState> = _celebrationState.asStateFlow()

    fun dismissCelebration() {
        _celebrationState.value = CelebrationState.None
    }

    private val triggeredMilestonesThisSession = mutableSetOf<Int>()
    private val notificationMilestones = setOf(7, 14, 30, 50)

    private suspend fun computeAndTriggerCelebration() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val days = dao.getDistinctDaysOnce().map { LocalDate.parse(it.day) }.toHashSet()
        var currentStreak = 0
        var d = today
        while (d in days) { currentStreak++; d = d.minusDays(1) }

        if (appPrefs.streakMilestonesEnabled.first()) {
            val notifMilestone = notificationMilestones.firstOrNull {
                currentStreak == it && it !in triggeredMilestonesThisSession
            }
            if (notifMilestone != null) {
                BurritoNotificationManager.sendStreakMilestoneNotification(context, notifMilestone)
            }
        }

        val milestone = listOf(100, 30, 7).firstOrNull {
            currentStreak == it && it !in triggeredMilestonesThisSession
        }
        if (milestone != null) {
            triggeredMilestonesThisSession.add(milestone)
            _celebrationState.value = CelebrationState.StreakMilestone(milestone)
        } else {
            _celebrationState.value = CelebrationState.EmojiRain
        }
    }

    sealed interface BurritoVerdictState {
        data object None : BurritoVerdictState
        data object Classifying : BurritoVerdictState
        data class Verdict(val isBurrito: Boolean, val confidence: Float, val comment: String) : BurritoVerdictState
        data class RateLimited(val secondsRemaining: Int) : BurritoVerdictState
        data object BrainBroke : BurritoVerdictState
    }

    private val _verdictState = MutableStateFlow<BurritoVerdictState>(BurritoVerdictState.None)
    val verdictState: StateFlow<BurritoVerdictState> = _verdictState.asStateFlow()

    fun classifyPhoto(photoUri: Uri) {
        _verdictState.value = BurritoVerdictState.Classifying
        viewModelScope.launch {
            _verdictState.value = when (val outcome = burritoClassifier.classify(photoUri)) {
                is ClassificationOutcome.Success -> BurritoVerdictState.Verdict(outcome.isBurrito, outcome.confidence, outcome.comment)
                is ClassificationOutcome.RateLimited -> BurritoVerdictState.RateLimited(outcome.secondsRemaining)
                is ClassificationOutcome.ApiError -> BurritoVerdictState.BrainBroke
                is ClassificationOutcome.Failure -> BurritoVerdictState.BrainBroke
            }
        }
    }

    fun dismissVerdict() {
        _verdictState.value = BurritoVerdictState.None
    }

    private fun buildFavoritePlace(entries: List<BurritoEntry>): Triple<String?, Double?, Double?> {
        val favoriteName = entries.mostFrequentLocationName() ?: return Triple(null, null, null)
        val entry = entries.first { it.locationName == favoriteName }
        return Triple(favoriteName, entry.locationLat, entry.locationLong)
    }

    private fun List<BurritoEntry>.mostFrequentLocationName(): String? =
        mapNotNull { it.locationName }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

    private fun buildDailyCounts(entries: List<BurritoEntry>, today: LocalDate): List<Int> {
        val zone = ZoneId.systemDefault()
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
        val totalCount: Int,
    )

    sealed interface TimeScreenUIState {
        data object Loading : TimeScreenUIState
        data class Error(val throwable: Throwable) : TimeScreenUIState
        data class Success(val data: TimeScreenData) : TimeScreenUIState
    }
}
