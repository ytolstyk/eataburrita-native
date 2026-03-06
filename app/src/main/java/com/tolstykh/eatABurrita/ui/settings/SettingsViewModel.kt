package com.tolstykh.eatABurrita.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolstykh.eatABurrita.data.AppPreferencesRepository
import com.tolstykh.eatABurrita.data.BurritoDao
import com.tolstykh.eatABurrita.data.BurritoEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dao: BurritoDao,
    private val appPrefs: AppPreferencesRepository,
) : ViewModel() {

    val entries: StateFlow<List<BurritoEntry>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isDarkMode: StateFlow<Boolean> = appPrefs.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleDarkMode(dark: Boolean) {
        viewModelScope.launch { appPrefs.setDarkMode(dark) }
    }

    fun addEntry(timestamp: Long) {
        viewModelScope.launch { dao.insert(BurritoEntry(timestamp = timestamp)) }
    }

    fun updateEntry(entry: BurritoEntry) {
        viewModelScope.launch { dao.update(entry) }
    }

    fun deleteEntry(entry: BurritoEntry) {
        viewModelScope.launch { dao.delete(entry) }
    }

    fun deleteAll() {
        viewModelScope.launch { dao.deleteAll() }
    }
}
