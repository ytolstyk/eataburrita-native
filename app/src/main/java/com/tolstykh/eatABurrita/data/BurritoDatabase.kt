package com.tolstykh.eatABurrita.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [BurritoEntry::class], version = 2)
abstract class BurritoDatabase : RoomDatabase() {
    abstract fun burritoDao(): BurritoDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE burrito_entries ADD COLUMN locationLat REAL")
                db.execSQL("ALTER TABLE burrito_entries ADD COLUMN locationLong REAL")
                db.execSQL("ALTER TABLE burrito_entries ADD COLUMN locationName TEXT")
            }
        }
    }
}
