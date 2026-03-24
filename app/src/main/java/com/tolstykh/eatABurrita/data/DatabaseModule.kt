package com.tolstykh.eatABurrita.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BurritoDatabase =
        Room.databaseBuilder(context, BurritoDatabase::class.java, "burrito_db")
            .addMigrations(BurritoDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideDao(db: BurritoDatabase): BurritoDao = db.burritoDao()
}
