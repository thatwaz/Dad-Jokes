package com.thatwaz.dadjokes.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.thatwaz.dadjokes.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SettingsDataStore {


    private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("use_dynamic_color")

    suspend fun saveUseDynamicColor(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    val useDynamicColorFlow: (Context) -> Flow<Boolean> = { context ->
        context.dataStore.data
            .map { preferences ->
                preferences[DYNAMIC_COLOR_KEY] ?: true // Default is true
            }
    }
}
