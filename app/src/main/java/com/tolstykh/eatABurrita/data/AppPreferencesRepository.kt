package com.tolstykh.eatABurrita.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.tolstykh.eatABurrita.appPrefsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val showLocationModalKey = booleanPreferencesKey("show_location_modal")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val notificationPermissionAskedKey = booleanPreferencesKey("notification_permission_asked")
    private val threeDayNotifiedKey = booleanPreferencesKey("three_day_notified")
    private val sevenDayNotifiedKey = booleanPreferencesKey("seven_day_notified")
    private val checkedIngredientsKey = stringSetPreferencesKey("checked_ingredients")
    private val unlockedAchievementsKey = stringSetPreferencesKey("unlocked_achievements")
    private val geofenceEnabledKey = booleanPreferencesKey("geofence_enabled")
    private val geofenceLastNotifiedKey = stringSetPreferencesKey("geofence_last_notified")
    private val streakMilestonesEnabledKey = booleanPreferencesKey("streak_milestones_enabled")
    private val weeklyRecapEnabledKey = booleanPreferencesKey("weekly_recap_enabled")

    val isDarkMode: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[darkModeKey] ?: false }

    val showLocationModal: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[showLocationModalKey] ?: true }

    val notificationsEnabled: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[notificationsEnabledKey] ?: true }

    val notificationPermissionAsked: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[notificationPermissionAskedKey] ?: false }

    val threeDayNotified: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[threeDayNotifiedKey] ?: false }

    val sevenDayNotified: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[sevenDayNotifiedKey] ?: false }

    val checkedIngredients: Flow<Set<String>> = context.appPrefsDataStore.data
        .map { prefs -> prefs[checkedIngredientsKey] ?: emptySet() }

    val unlockedAchievements: Flow<Set<String>> = context.appPrefsDataStore.data
        .map { prefs -> prefs[unlockedAchievementsKey] ?: emptySet() }

    val geofenceEnabled: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[geofenceEnabledKey] ?: false }

    val streakMilestonesEnabled: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[streakMilestonesEnabledKey] ?: true }

    val weeklyRecapEnabled: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[weeklyRecapEnabledKey] ?: true }

    suspend fun setDarkMode(dark: Boolean) {
        context.appPrefsDataStore.edit { prefs ->
            prefs[darkModeKey] = dark
        }
    }

    suspend fun setShowLocationModal(show: Boolean) {
        context.appPrefsDataStore.edit { prefs ->
            prefs[showLocationModalKey] = show
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.appPrefsDataStore.edit { prefs ->
            prefs[notificationsEnabledKey] = enabled
        }
    }

    suspend fun setNotificationPermissionAsked() {
        context.appPrefsDataStore.edit { prefs ->
            prefs[notificationPermissionAskedKey] = true
        }
    }

    suspend fun setThreeDayNotified(notified: Boolean) {
        context.appPrefsDataStore.edit { prefs ->
            prefs[threeDayNotifiedKey] = notified
        }
    }

    suspend fun setSevenDayNotified(notified: Boolean) {
        context.appPrefsDataStore.edit { prefs ->
            prefs[sevenDayNotifiedKey] = notified
        }
    }

    suspend fun toggleIngredient(recipeId: Int, index: Int) {
        val key = "${recipeId}_${index}"
        context.appPrefsDataStore.edit { prefs ->
            val current = prefs[checkedIngredientsKey] ?: emptySet()
            prefs[checkedIngredientsKey] = if (key in current) current - key else current + key
        }
    }

    suspend fun uncheckAllIngredients(recipeId: Int) {
        context.appPrefsDataStore.edit { prefs ->
            val current = prefs[checkedIngredientsKey] ?: emptySet()
            prefs[checkedIngredientsKey] = current.filterNot { it.startsWith("${recipeId}_") }.toSet()
        }
    }

    suspend fun markAchievementsUnlocked(ids: Set<String>) {
        context.appPrefsDataStore.edit { prefs ->
            val current = prefs[unlockedAchievementsKey] ?: emptySet()
            prefs[unlockedAchievementsKey] = current + ids
        }
    }

    suspend fun resetNotificationSentFlags() {
        context.appPrefsDataStore.edit { prefs ->
            prefs[threeDayNotifiedKey] = false
            prefs[sevenDayNotifiedKey] = false
        }
    }

    suspend fun setGeofenceEnabled(enabled: Boolean) {
        context.appPrefsDataStore.edit { prefs -> prefs[geofenceEnabledKey] = enabled }
    }

    suspend fun recordGeofenceNotified(locationName: String, timestampMillis: Long) {
        context.appPrefsDataStore.edit { prefs ->
            val current = prefs[geofenceLastNotifiedKey] ?: emptySet()
            val filtered = current.filterNot { it.startsWith("$locationName|") }.toSet()
            prefs[geofenceLastNotifiedKey] = filtered + "$locationName|$timestampMillis"
        }
    }

    suspend fun getGeofenceLastNotifiedTime(locationName: String): Long {
        val set = context.appPrefsDataStore.data
            .map { prefs -> prefs[geofenceLastNotifiedKey] ?: emptySet() }
            .first()
        return set.firstOrNull { it.startsWith("$locationName|") }
            ?.removePrefix("$locationName|")
            ?.toLongOrNull() ?: 0L
    }

    suspend fun setStreakMilestonesEnabled(enabled: Boolean) {
        context.appPrefsDataStore.edit { prefs -> prefs[streakMilestonesEnabledKey] = enabled }
    }

    suspend fun setWeeklyRecapEnabled(enabled: Boolean) {
        context.appPrefsDataStore.edit { prefs -> prefs[weeklyRecapEnabledKey] = enabled }
    }
}
