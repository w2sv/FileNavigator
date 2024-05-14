package com.w2sv.datastorage.preferences.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.preferences_datastore.PreferencesDataStoreRepository
import com.w2sv.androidutils.generic.dynamicColorsSupported
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(dataStore: DataStore<Preferences>) :
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
}