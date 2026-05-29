package com.w2sv.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.persistedpreferences.EnumSavePolicy
import com.w2sv.persistedpreferences.PersistedPreferencesAccessor
import javax.inject.Inject
import javax.inject.Singleton
import slimber.log.i

@Singleton
internal class PreferencesRepositoryImpl @Inject constructor(dataStore: DataStore<Preferences>) :
    PersistedPreferencesAccessor(dataStore, log = ::i),
    PreferencesRepository {

    override val theme = enumPreference("theme", { Theme.Default }, EnumSavePolicy.byOrdinal())

    override val useAmoledBlackTheme = persistedPreference(booleanPreferencesKey("useAmoledBlackTheme")) { false }

    override val useDynamicColors = persistedPreference(booleanPreferencesKey("useDynamicColors")) { dynamicColorsSupported }

    override val postNotificationsPermissionRequested = persistedPreference(
        booleanPreferencesKey("postNotificationsPermissionRequested")
    ) { false }

    override val showStorageVolumeNames = persistedPreference(
        booleanPreferencesKey("showStorageVolumeNames")
    ) { true }

    override val showAutoMoveIntroduction = persistedPreference(
        booleanPreferencesKey("showAutoMoveIntroduction")
    ) { true }

    override val showQuickMovePermissionQueryExplanation = persistedPreference(
        booleanPreferencesKey("showQuickMovePermissionQueryExplanation")
    ) { true }
}
