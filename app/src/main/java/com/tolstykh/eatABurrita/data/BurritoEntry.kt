package com.tolstykh.eatABurrita.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "burrito_entries")
data class BurritoEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val locationLat: Double? = null,
    val locationLong: Double? = null,
    val locationName: String? = null,
)
