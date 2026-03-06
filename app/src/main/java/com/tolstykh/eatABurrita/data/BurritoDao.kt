package com.tolstykh.eatABurrita.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BurritoDao {
    @Insert
    suspend fun insert(entry: BurritoEntry)

    @Query("SELECT COUNT(*) FROM burrito_entries")
    fun getCount(): Flow<Int>

    @Query("SELECT MAX(timestamp) FROM burrito_entries")
    fun getLatestTimestamp(): Flow<Long?>

    @Query("SELECT * FROM burrito_entries WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun getEntriesSince(since: Long): Flow<List<BurritoEntry>>

    @Query("SELECT * FROM burrito_entries ORDER BY timestamp DESC")
    fun getAll(): Flow<List<BurritoEntry>>

    @Delete
    suspend fun delete(entry: BurritoEntry)

    @Update
    suspend fun update(entry: BurritoEntry)

    @Query("DELETE FROM burrito_entries")
    suspend fun deleteAll()
}
