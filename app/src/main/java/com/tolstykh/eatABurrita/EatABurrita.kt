package com.tolstykh.eatABurrita

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.HiltAndroidApp

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_store")

@HiltAndroidApp
class EatABurrita: Application()
