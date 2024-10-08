package com.w2sv.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.datastoreutils.preferences.PreferencesDataStoreRepository
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PreferencesRepositoryImpl @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore),
    PreferencesRepository {

    override val theme = dataStoreFlow(
        intPreferencesKey("theme"),
        Theme.Default
    )

    override val useAmoledBlackTheme =
        dataStoreFlow(booleanPreferencesKey("useAmoledBlackTheme"), false)

    override val useDynamicColors = dataStoreFlow(
        booleanPreferencesKey("useDynamicColors"),
        dynamicColorsSupported
    )

    override val postNotificationsPermissionRequested = dataStoreFlow(
        booleanPreferencesKey("postNotificationsPermissionRequested"),
        false
    )

    override val showStorageVolumeNames: DataStoreFlow<Boolean> = dataStoreFlow(
        booleanPreferencesKey("showStorageVolumeNames"),
        false
    )

    override val showAutoMoveIntroduction: DataStoreFlow<Boolean> = dataStoreFlow(
        booleanPreferencesKey("showAutoMoveIntroduction"),
        true
    )
}