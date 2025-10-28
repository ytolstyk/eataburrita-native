package com.tolstykh.eatABurrita

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_store")

val BURRITO_COUNTER_KEY: Preferences.Key<Int> = intPreferencesKey("burrito_counter")
val TIMESTAMP_KEY = longPreferencesKey("timestamp")

class StoreManager(private val context: Context) {
    val burritoCount: Flow<Int> = context.appDataStore.data.map { store ->
        store[BURRITO_COUNTER_KEY] ?: 0
    }

    val timestamp: Flow<Long> = context.appDataStore.data.map { store ->
        store[TIMESTAMP_KEY] ?: 0L
    }

    suspend fun addBurritoCount() {
        context.appDataStore.edit { store ->
            val currentCounterValue = store[BURRITO_COUNTER_KEY] ?: 0
            store[BURRITO_COUNTER_KEY] = currentCounterValue + 1
        }
    }

    suspend fun updateTimestamp() {
        context.appDataStore.edit { store ->
            store[TIMESTAMP_KEY] = Instant.now().toEpochMilli()
        }
    }
}
