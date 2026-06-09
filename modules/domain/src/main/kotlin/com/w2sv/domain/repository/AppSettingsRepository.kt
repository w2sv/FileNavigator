package com.w2sv.domain.repository

import com.w2sv.domain.model.settings.AppSettings
import kotlinx.coroutines.flow.StateFlow

interface AppSettingsRepository {
    val appSettings: StateFlow<AppSettings>
    suspend fun updateAppSettings(transform: (AppSettings) -> AppSettings)
}
