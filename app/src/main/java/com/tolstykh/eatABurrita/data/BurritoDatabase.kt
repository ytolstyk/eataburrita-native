package com.tolstykh.eatABurrita.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [BurritoEntry::class, RestaurantNote::class], version = 5)
abstract class BurritoDatabase : RoomDatabase() {
    abstract fun burritoDao(): BurritoDao
    abstract fun restaurantNoteDao(): RestaurantNoteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE burrito_entries ADD COLUMN locationLat REAL")
                db.execSQL("ALTER TABLE burrito_entries ADD COLUMN locationLong REAL")
                db.execSQL("ALTER TABLE burrito_entries ADD COLUMN locationName TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE burrito_entries ADD COLUMN calories INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE burrito_entries ADD COLUMN photoPath TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `restaurant_notes` (
                        `placeId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `personalRating` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `hideFromMap` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`placeId`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
