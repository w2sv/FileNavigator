package com.w2sv.data.storage.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.data.model.StorageAccessStatus
import com.w2sv.data.model.Theme
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore) {

    // =======================
    // Internally set flags
    // =======================

    val showedManageExternalStorageRational: Flow<Boolean> =
        getFlow(Key.SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL, false)

    suspend fun saveShowedManageExternalStorageRational() {
        save(Key.SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL, true)
    }

    val showedPostNotificationsPermissionsRational: Flow<Boolean> =
        getFlow(Key.SHOWED_POST_NOTIFICATIONS_PERMISSION_RATIONAL, false)

    suspend fun saveShowedPostNotificationsPermissionsRational() {
        save(Key.SHOWED_POST_NOTIFICATIONS_PERMISSION_RATIONAL, true)
    }

    // =======================
    // User Preferences
    // =======================

    val disableListenerOnLowBattery: Flow<Boolean> =
        getFlow(Key.DISABLE_LISTENER_ON_LOW_BATTERY, false)

    suspend fun saveDisableListenerOnLowBattery(value: Boolean) {
        save(Key.DISABLE_LISTENER_ON_LOW_BATTERY, value)
    }

    val theme: Flow<Theme> = getEnumFlow(
        Key.THEME,
        Theme.DeviceDefault
    )

    suspend fun saveTheme(value: Theme) {
        save(Key.THEME, value)
    }

    // =======================
    // Other
    // =======================

    val previousStorageAccessStatus: Flow<StorageAccessStatus> = getEnumFlow(
        Key.PREVIOUS_STORAGE_ACCESS_STATUS,
        StorageAccessStatus.NoAccess
    )

    suspend fun savePreviousStorageAccessStatus(value: StorageAccessStatus) {
        save(Key.PREVIOUS_STORAGE_ACCESS_STATUS, value)
    }

    private object Key {
        val SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL =
            booleanPreferencesKey("showedManageExternalStorageRational")

        val SHOWED_POST_NOTIFICATIONS_PERMISSION_RATIONAL =
            booleanPreferencesKey("showedPostNotificationsPermissionRational")

        val DISABLE_LISTENER_ON_LOW_BATTERY =
            booleanPreferencesKey("disableListenerOnLowBattery")

        val PREVIOUS_STORAGE_ACCESS_STATUS = intPreferencesKey(
            "previousStorageAccessStatus"
        )

        val THEME = intPreferencesKey(
            "inAppTheme"
        )
    }
}