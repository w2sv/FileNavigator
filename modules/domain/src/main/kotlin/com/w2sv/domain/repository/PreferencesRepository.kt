package com.w2sv.domain.repository

import com.w2sv.domain.model.Theme
import com.w2sv.persistedpreferences.PersistedPreference

interface PreferencesRepository {
    val theme: PersistedPreference<Theme>
    val useAmoledBlackTheme: PersistedPreference<Boolean>
    val useDynamicColors: PersistedPreference<Boolean>
    val postNotificationsPermissionRequested: PersistedPreference<Boolean>
    val showStorageVolumeNames: PersistedPreference<Boolean>
    val showAutoMoveIntroduction: PersistedPreference<Boolean>
    val showQuickMovePermissionQueryExplanation: PersistedPreference<Boolean>
}
