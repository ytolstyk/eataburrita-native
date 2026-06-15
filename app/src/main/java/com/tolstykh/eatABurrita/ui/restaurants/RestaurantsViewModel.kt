package com.tolstykh.eatABurrita.ui.restaurants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tolstykh.eatABurrita.data.RestaurantNote
import com.tolstykh.eatABurrita.data.RestaurantNoteDao
import com.tolstykh.eatABurrita.data.upsertPreservingCreatedAt
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RestaurantsViewModel @Inject constructor(
    private val dao: RestaurantNoteDao,
) : ViewModel() {

    val notes: StateFlow<List<RestaurantNote>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun upsert(draft: RestaurantNote) = viewModelScope.launch {
        dao.upsertPreservingCreatedAt(draft)
    }

    fun delete(note: RestaurantNote) = viewModelScope.launch {
        dao.delete(note)
    }
}
