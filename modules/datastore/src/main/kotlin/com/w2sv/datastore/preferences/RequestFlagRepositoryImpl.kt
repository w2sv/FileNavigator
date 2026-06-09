package com.w2sv.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.domain.repository.RequestFlagRepository
import com.w2sv.persistedpreferences.PersistedPreferencesAccessor
import javax.inject.Inject
import javax.inject.Singleton
import slimber.log.i

@Singleton
internal class RequestFlagRepositoryImpl @Inject constructor(dataStore: DataStore<Preferences>) :
    PersistedPreferencesAccessor(dataStore, log = ::i),
    RequestFlagRepository {

    override val postNotificationsPermissionRequested = persistedPreference(
        booleanPreferencesKey("postNotificationsPermissionRequested")
    ) { false }

    override val showAutoMoveIntroduction = persistedPreference(
        booleanPreferencesKey("showAutoMoveIntroduction")
    ) { true }

    override val showQuickMovePermissionQueryExplanation = persistedPreference(
        booleanPreferencesKey("showQuickMovePermissionQueryExplanation")
    ) { true }
}
