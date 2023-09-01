package com.w2sv.filenavigator.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.filenavigator.ui.components.Theme
import com.w2sv.filenavigator.utils.StorageAccessStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
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

    val inAppTheme: Flow<Theme> = getEnumFlow(
        Key.IN_APP_THEME,
        Theme.DeviceDefault
    )

    suspend fun saveInAppTheme(value: Theme) {
        save(Key.IN_APP_THEME, value)
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

        val IN_APP_THEME = intPreferencesKey(
            "inAppTheme"
        )
    }
}