package com.rejown.howblurworks.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val themeMode: Int = 0, // 0 = Auto, 1 = Light, 2 = Dark
    val defaultDuration: Int = 1, // 0 = 15s, 1 = 30s, 2 = 60s
    val showKernelOverlay: Boolean = true,
    val showPixelInfo: Boolean = true,
    val showScanline: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val DEFAULT_DURATION = intPreferencesKey("default_duration")
        val SHOW_KERNEL_OVERLAY = booleanPreferencesKey("show_kernel_overlay")
        val SHOW_PIXEL_INFO = booleanPreferencesKey("show_pixel_info")
        val SHOW_SCANLINE = booleanPreferencesKey("show_scanline")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: 0,
            defaultDuration = preferences[PreferencesKeys.DEFAULT_DURATION] ?: 1,
            showKernelOverlay = preferences[PreferencesKeys.SHOW_KERNEL_OVERLAY] ?: true,
            showPixelInfo = preferences[PreferencesKeys.SHOW_PIXEL_INFO] ?: true,
            showScanline = preferences[PreferencesKeys.SHOW_SCANLINE] ?: true
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

    suspend fun updateShowKernelOverlay(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_KERNEL_OVERLAY] = show
        }
    }

    suspend fun updateShowPixelInfo(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_PIXEL_INFO] = show
        }
    }

    suspend fun updateShowScanline(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SCANLINE] = show
        }
    }
}
