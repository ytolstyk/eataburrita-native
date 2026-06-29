package com.tolstykh.eatABurrita.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuScanDao {
    @Query("SELECT * FROM menu_scans ORDER BY scannedAt DESC")
    fun getAll(): Flow<List<MenuScan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: MenuScan): Long

    @Delete
    suspend fun delete(scan: MenuScan)
}
