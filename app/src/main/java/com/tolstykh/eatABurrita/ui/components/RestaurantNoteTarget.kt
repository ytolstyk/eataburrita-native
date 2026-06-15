package com.tolstykh.eatABurrita.ui.components

import com.google.android.libraries.places.api.model.Place
import com.tolstykh.eatABurrita.data.RestaurantNote

sealed class RestaurantNoteTarget {
    data class FromMap(val place: Place, val existingNote: RestaurantNote?) : RestaurantNoteTarget()
    data class FromList(val existingNote: RestaurantNote) : RestaurantNoteTarget()
}
