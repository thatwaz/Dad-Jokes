package com.thatwaz.dadjokes.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PrefsRepo @Inject constructor(private val context: Context) {


    private val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
    private val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")

    @RequiresApi(Build.VERSION_CODES.O)
    val notificationTimeFlow: Flow<LocalTime> = context.dataStore.data
        .map { prefs ->
            val hour = prefs[NOTIFICATION_HOUR] ?: 9
            val minute = prefs[NOTIFICATION_MINUTE] ?: 0
            LocalTime.of(hour, minute)
        }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveNotificationTime(time: LocalTime) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_HOUR] = time.hour
            prefs[NOTIFICATION_MINUTE] = time.minute
        }
    }
}

