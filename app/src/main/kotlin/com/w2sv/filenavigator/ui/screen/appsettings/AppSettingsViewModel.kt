package com.w2sv.filenavigator.ui.screen.appsettings

import androidx.lifecycle.viewModelScope
import com.w2sv.common.logging.LoggingViewModel
import com.w2sv.domain.model.settings.AppSettings
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AppSettingsViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository) : LoggingViewModel() {

    val appSettings by preferencesRepository::appSettings

    fun saveAppSettings(appSettings: AppSettings) {
        viewModelScope.launch { preferencesRepository.saveAppSettings(appSettings) }
    }
}
