package com.w2sv.domain.repository

import com.w2sv.domain.model.settings.AppSettings
import com.w2sv.persistedpreferences.PersistedPreference
import kotlinx.coroutines.flow.StateFlow

interface PreferencesRepository {
    val appSettings: StateFlow<AppSettings>
    val postNotificationsPermissionRequested: PersistedPreference<Boolean>
    val showAutoMoveIntroduction: PersistedPreference<Boolean>
    val showQuickMovePermissionQueryExplanation: PersistedPreference<Boolean>
    suspend fun saveAppSettings(appSettings: AppSettings)
}
