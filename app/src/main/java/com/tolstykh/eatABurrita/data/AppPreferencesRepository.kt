package com.tolstykh.eatABurrita.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.tolstykh.eatABurrita.appPrefsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val showLocationModalKey = booleanPreferencesKey("show_location_modal")

    val isDarkMode: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[darkModeKey] ?: false }

    val showLocationModal: Flow<Boolean> = context.appPrefsDataStore.data
        .map { prefs -> prefs[showLocationModalKey] ?: true }

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
}
