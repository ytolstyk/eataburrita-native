package com.tolstykh.eatABurrita.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_scans")
data class MenuScan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restaurantName: String,
    val scannedAt: Long = System.currentTimeMillis(),
    val itemsJson: String,
)
