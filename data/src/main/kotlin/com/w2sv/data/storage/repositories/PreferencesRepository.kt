package com.w2sv.data.storage.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.data.model.StorageAccessStatus
import com.w2sv.data.model.Theme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore) {

    val disableNavigatorOnLowBattery = getPersistedValue(
        booleanPreferencesKey("disableNavigatorOnLowBattery"),
        true
    )

    val theme = getPersistedValue(
        intPreferencesKey("theme"),
        Theme.DeviceDefault
    )

    // =======================
    // Other
    // =======================

    val postNotificationsPermissionRequested = getPersistedValue(
        booleanPreferencesKey("postNotificationsPermissionRequested"),
        false
    )

    val priorStorageAccessStatus = getPersistedValue(
        intPreferencesKey("priorStorageAccessStatus"),
        StorageAccessStatus.NoAccess
    )
}