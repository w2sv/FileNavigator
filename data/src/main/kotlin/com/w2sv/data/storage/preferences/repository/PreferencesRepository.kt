package com.w2sv.data.storage.preferences.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.DataStoreRepository
import com.w2sv.domain.model.Theme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(dataStore: DataStore<Preferences>) :
    DataStoreRepository(dataStore) {

    val theme = dataStoreFlow(
        intPreferencesKey("theme"),
        Theme.DeviceDefault
    )

    val useDynamicColors = dataStoreFlow(
        booleanPreferencesKey("useDynamicColors"),
        false
    )

    val postNotificationsPermissionRequested = dataStoreFlow(
        booleanPreferencesKey("postNotificationsPermissionRequested"),
        false
    )
}