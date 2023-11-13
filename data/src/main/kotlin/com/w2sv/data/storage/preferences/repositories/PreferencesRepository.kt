package com.w2sv.data.storage.preferences.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.data.model.StorageAccessStatus
import com.w2sv.data.model.Theme
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
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

    val useDynamicColors = getPersistedValue(
        booleanPreferencesKey("useDynamicColors"),
        false
    )

    val navigatorStartDateTime =
        getNullableFlow(
            Key.navigatorStartDateTime,
            null
        )
            .map {
                it?.let { LocalDateTime.parse(it) }
            }

    suspend fun saveNavigatorStartDateTime(dateTime: LocalDateTime = LocalDateTime.now()) {
        save(Key.navigatorStartDateTime, dateTime.toString())
    }

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

    private object Key {
        val navigatorStartDateTime = stringPreferencesKey("navigatorStartDateTime")
    }
}