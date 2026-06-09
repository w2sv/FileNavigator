package com.w2sv.filenavigator.ui.screen.appsettings

import androidx.lifecycle.viewModelScope
import com.w2sv.core.logging.LoggingViewModel
import com.w2sv.domain.model.settings.AppSettings
import com.w2sv.domain.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AppSettingsViewModel @Inject constructor(private val appSettingsRepository: AppSettingsRepository) : LoggingViewModel() {

    val appSettings by appSettingsRepository::appSettings

    fun updateAppSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { appSettingsRepository.updateAppSettings(transform) }
    }
}
