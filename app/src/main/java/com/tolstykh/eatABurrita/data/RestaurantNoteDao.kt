package com.tolstykh.eatABurrita.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantNoteDao {
    @Query("SELECT * FROM restaurant_notes ORDER BY name ASC")
    fun getAll(): Flow<List<RestaurantNote>>

    @Query("SELECT placeId FROM restaurant_notes WHERE hideFromMap = 1")
    fun getHiddenPlaceIds(): Flow<List<String>>

    @Query("SELECT * FROM restaurant_notes WHERE placeId = :placeId")
    suspend fun getByPlaceId(placeId: String): RestaurantNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: RestaurantNote)

    @Delete
    suspend fun delete(note: RestaurantNote)
}

suspend fun RestaurantNoteDao.upsertPreservingCreatedAt(draft: RestaurantNote) {
    val existing = getByPlaceId(draft.placeId)
    upsert(draft.copy(createdAt = existing?.createdAt ?: draft.createdAt))
}
