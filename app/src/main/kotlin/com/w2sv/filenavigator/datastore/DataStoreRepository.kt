package com.w2sv.filenavigator.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.filenavigator.mediastore.FileType
import com.w2sv.filenavigator.ui.Theme
import com.w2sv.filenavigator.utils.StorageAccessStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreRepository @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore) {

    val fileTypeStatus: Map<FileType, Flow<FileType.Status>> = getEnumValuedFlowMap(FileType.all)

    val fileSourceEnabled: Map<FileType.Source, Flow<Boolean>> = getFlowMap(
        FileType.all
            .map { it.sources }
            .flatten()
    )

    val showedManageExternalStorageRational: Flow<Boolean> =
        getFlow(PreferencesKey.SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL, false)

    val showedPostNotificationsPermissionsRational: Flow<Boolean> =
        getFlow(PreferencesKey.SHOWED_POST_NOTIFICATIONS_PERMISSION_RATIONAL, false)

    val disableListenerOnLowBattery: Flow<Boolean> =
        getFlow(PreferencesKey.DISABLE_LISTENER_ON_LOW_BATTERY, false)

    val previousStorageAccessStatus: Flow<StorageAccessStatus> = getEnumFlow(
        PreferencesKey.PREVIOUS_STORAGE_ACCESS_STATUS,
        StorageAccessStatus.NoAccess
    )

    val inAppTheme: Flow<Theme> = getEnumFlow(
        PreferencesKey.IN_APP_THEME,
        Theme.DeviceDefault
    )
}