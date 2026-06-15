package com.tolstykh.eatABurrita.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurant_notes")
data class RestaurantNote(
    @PrimaryKey val placeId: String,
    val name: String,
    val personalRating: Int = 0,
    val notes: String = "",
    val hideFromMap: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
