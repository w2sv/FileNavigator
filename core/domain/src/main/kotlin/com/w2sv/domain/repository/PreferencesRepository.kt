package com.w2sv.domain.repository

import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.datastoreutils.datastoreflow.DataStoreStateFlow
import com.w2sv.domain.model.Theme

interface PreferencesRepository {
    val theme: DataStoreFlow<Theme>
    val useAmoledBlackTheme: DataStoreFlow<Boolean>
    val useDynamicColors: DataStoreFlow<Boolean>
    val postNotificationsPermissionRequested: DataStoreFlow<Boolean>
    val showStorageVolumeNames: DataStoreFlow<Boolean>
    val showAutoMoveIntroduction: DataStoreFlow<Boolean>
    val showQuickMovePermissionQueryExplanation: DataStoreStateFlow<Boolean>
}