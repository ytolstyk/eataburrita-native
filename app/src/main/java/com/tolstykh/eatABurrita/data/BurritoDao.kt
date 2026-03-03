package com.tolstykh.eatABurrita.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BurritoDao {
    @Insert
    suspend fun insert(entry: BurritoEntry)

    @Query("SELECT COUNT(*) FROM burrito_entries")
    fun getCount(): Flow<Int>

    @Query("SELECT MAX(timestamp) FROM burrito_entries")
    fun getLatestTimestamp(): Flow<Long?>
}
