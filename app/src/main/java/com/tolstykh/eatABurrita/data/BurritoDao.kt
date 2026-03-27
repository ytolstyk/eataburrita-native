package com.tolstykh.eatABurrita.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Lightweight query result types — never load full entries for aggregated stats
data class DowCount(
    @ColumnInfo(name = "dow") val dow: Int,   // 0=Sun..6=Sat (SQLite %w)
    @ColumnInfo(name = "cnt") val cnt: Int,
)
data class HourCount(
    @ColumnInfo(name = "hour") val hour: Int,
    @ColumnInfo(name = "cnt") val cnt: Int,
)
data class LocationCount(
    @ColumnInfo(name = "locationName") val locationName: String,
    @ColumnInfo(name = "cnt") val cnt: Int,
)
data class MonthCount(
    @ColumnInfo(name = "month") val month: String, // "YYYY-MM"
    @ColumnInfo(name = "cnt") val cnt: Int,
)
data class DayString(
    @ColumnInfo(name = "day") val day: String, // "YYYY-MM-DD"
)

@Dao
interface BurritoDao {
    @Insert
    suspend fun insert(entry: BurritoEntry)

    @Query("SELECT COUNT(*) FROM burrito_entries")
    fun getCount(): Flow<Int>

    @Query("SELECT MAX(timestamp) FROM burrito_entries")
    fun getLatestTimestamp(): Flow<Long?>

    @Query("SELECT MAX(timestamp) FROM burrito_entries")
    suspend fun getLatestTimestampOnce(): Long?

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

    @Query("SELECT * FROM burrito_entries WHERE locationLat IS NOT NULL ORDER BY timestamp DESC")
    fun getEntriesWithLocation(): Flow<List<BurritoEntry>>

    @Query("SELECT * FROM burrito_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd AND locationLat IS NOT NULL ORDER BY timestamp ASC")
    suspend fun getEntriesWithLocationForDay(dayStart: Long, dayEnd: Long): List<BurritoEntry>

    @Query("SELECT COUNT(*) FROM burrito_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    suspend fun getCountForDay(dayStart: Long, dayEnd: Long): Int

    // Aggregated stats queries — let SQLite do the work instead of loading all rows

    @Query("SELECT CAST(strftime('%w', datetime(timestamp/1000, 'unixepoch', 'localtime')) AS INTEGER) as dow, COUNT(*) as cnt FROM burrito_entries GROUP BY dow")
    fun getDayOfWeekCounts(): Flow<List<DowCount>>

    @Query("SELECT CAST(strftime('%H', datetime(timestamp/1000, 'unixepoch', 'localtime')) AS INTEGER) as hour, COUNT(*) as cnt FROM burrito_entries GROUP BY hour")
    fun getHourOfDayCounts(): Flow<List<HourCount>>

    @Query("SELECT locationName, COUNT(*) as cnt FROM burrito_entries WHERE locationName IS NOT NULL GROUP BY locationName ORDER BY cnt DESC LIMIT 5")
    fun getTopLocations(): Flow<List<LocationCount>>

    @Query("SELECT strftime('%Y-%m', datetime(timestamp/1000, 'unixepoch', 'localtime')) as month, COUNT(*) as cnt FROM burrito_entries WHERE timestamp >= :since GROUP BY month ORDER BY month ASC")
    fun getMonthlyCounts(since: Long): Flow<List<MonthCount>>

    @Query("SELECT DISTINCT date(datetime(timestamp/1000, 'unixepoch', 'localtime')) as day FROM burrito_entries ORDER BY day ASC")
    fun getDistinctDays(): Flow<List<DayString>>
}
