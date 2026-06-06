package com.tolstykh.eatABurrita.ui.memories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolstykh.eatABurrita.data.BurritoDao
import com.tolstykh.eatABurrita.data.BurritoEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MemoriesViewModel @Inject constructor(
    private val dao: BurritoDao,
) : ViewModel() {

    val entries: StateFlow<List<BurritoEntry>> = dao.getEntriesWithPhoto()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deletePhoto(entry: BurritoEntry) {
        viewModelScope.launch {
            entry.photoPath?.let { path -> runCatching { File(path).delete() } }
            dao.update(entry.copy(photoPath = null))
        }
    }
}
