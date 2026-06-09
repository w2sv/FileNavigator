package com.w2sv.domain.repository

import com.w2sv.persistedpreferences.PersistedPreference

interface RequestFlagRepository {
    val postNotificationsPermissionRequested: PersistedPreference<Boolean>
    val showAutoMoveIntroduction: PersistedPreference<Boolean>
    val showQuickMovePermissionQueryExplanation: PersistedPreference<Boolean>
}
