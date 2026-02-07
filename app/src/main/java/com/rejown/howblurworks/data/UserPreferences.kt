package com.rejown.howblurworks.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val themeMode: Int = 0, // 0 = Auto, 1 = Light, 2 = Dark
    val defaultDuration: Int = 2 // 0 = 5s, 1 = 15s, 2 = 30s, 3 = 45s
) {
    /**
     * Get the duration in seconds based on the defaultDuration index
     */
    val durationSeconds: Int
        get() = when (defaultDuration) {
            0 -> 5
            1 -> 15
            2 -> 30
            3 -> 45
            else -> 30
        }
}

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val DEFAULT_DURATION = intPreferencesKey("default_duration")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: 0,
            defaultDuration = preferences[PreferencesKeys.DEFAULT_DURATION] ?: 2
        )
    }

    suspend fun updateThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun updateDefaultDuration(duration: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_DURATION] = duration
        }
    }
}
