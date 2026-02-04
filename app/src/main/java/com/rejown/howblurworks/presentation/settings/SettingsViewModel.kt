package com.rejown.howblurworks.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rejown.howblurworks.data.AppSettings
import com.rejown.howblurworks.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun updateThemeMode(mode: Int) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun updateDefaultDuration(duration: Int) {
        viewModelScope.launch {
            repository.updateDefaultDuration(duration)
        }
    }

    fun updateShowKernelOverlay(show: Boolean) {
        viewModelScope.launch {
            repository.updateShowKernelOverlay(show)
        }
    }

    fun updateShowPixelInfo(show: Boolean) {
        viewModelScope.launch {
            repository.updateShowPixelInfo(show)
        }
    }

    fun updateShowScanline(show: Boolean) {
        viewModelScope.launch {
            repository.updateShowScanline(show)
        }
    }
}
