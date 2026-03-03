package com.tolstykh.eatABurrita.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BurritoEntry::class], version = 1)
abstract class BurritoDatabase : RoomDatabase() {
    abstract fun burritoDao(): BurritoDao
}
