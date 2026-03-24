package com.tolstykh.eatABurrita.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.tolstykh.eatABurrita.data.BurritoDao
import com.tolstykh.eatABurrita.location.GetLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MapScreenViewModel @Inject constructor(
    private val getLocationUseCase: GetLocationUseCase,
    private val dao: BurritoDao,
) : ViewModel() {
    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Loading)
    val viewState = _viewState.asStateFlow()

    var lastCameraPosition: CameraPosition? = null

    data class PlaceStats(val count: Int, val lastTimestamp: Long)

    val placeStats: StateFlow<Map<String, PlaceStats>> = dao.getEntriesWithLocation()
        .map { entries ->
            entries
                .filter { it.locationLat != null && it.locationLong != null }
                .groupBy { "%.4f,%.4f".format(it.locationLat, it.locationLong) }
                .mapValues { (_, group) ->
                    PlaceStats(
                        count = group.size,
                        lastTimestamp = group.maxOf { it.timestamp },
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun getBurritoCountForPlace(place: Place, stats: Map<String, PlaceStats>): Int =
        getPlaceStatsForPlace(place, stats)?.count ?: 0

    fun getPlaceStatsForPlace(place: Place, stats: Map<String, PlaceStats>): PlaceStats? {
        val loc = place.location ?: return null
        val key = "%.4f,%.4f".format(loc.latitude, loc.longitude)
        return stats[key]
    }

    fun handle(event: PermissionEvent) {
        when (event) {
            PermissionEvent.Granted -> {
                viewModelScope.launch {
                    getLocationUseCase.invoke().collect { location ->
                        _viewState.value = ViewState.Success(location)
                    }
                }
            }

            PermissionEvent.Revoked -> {
                _viewState.value = ViewState.RevokedPermissions
            }
        }
    }
}

sealed interface ViewState {
    object Loading : ViewState
    data class Success(val location: LatLng?) : ViewState
    object RevokedPermissions : ViewState
}

sealed interface PermissionEvent {
    object Granted : PermissionEvent
    object Revoked : PermissionEvent
}