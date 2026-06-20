package com.tolstykh.eatABurrita.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "burrito_entries", indices = [Index(value = ["timestamp"])])
data class BurritoEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val locationLat: Double? = null,
    val locationLong: Double? = null,
    val locationName: String? = null,
    val calories: Int? = null,
    val photoPath: String? = null,
)
