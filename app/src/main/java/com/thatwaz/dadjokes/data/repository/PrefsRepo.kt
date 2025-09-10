package com.thatwaz.dadjokes.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.thatwaz.dadjokes.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject

class PrefsRepo @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keys
    private val KEY_ONBOARDED = booleanPreferencesKey("onboarded")
    private val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
    private val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")

    // Onboarding flag
    val hasOnboarded: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[KEY_ONBOARDED] ?: false }

    suspend fun setOnboarded(done: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_ONBOARDED] = done }
    }

    // Notification time (O+)
    @RequiresApi(Build.VERSION_CODES.O)
    val notificationTimeFlow: Flow<LocalTime> =
        context.dataStore.data.map { prefs ->
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

