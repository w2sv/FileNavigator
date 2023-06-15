package com.w2sv.filenavigator.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.filenavigator.mediastore.FileType
import com.w2sv.filenavigator.ui.Theme
import com.w2sv.filenavigator.utils.manageExternalStoragePermissionRequired
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreRepository @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore) {

    val fileTypeEnabled: Map<FileType, Flow<Boolean>> = getFlowMap(FileType.all)

    val fileSourceEnabled: Map<FileType.Source, Flow<Boolean>> = getFlowMap(
        FileType.all
            .map { it.sources }
            .flatten()
    )

    val showedManageExternalStorageRational: Flow<Boolean> =
        getFlow(PreferencesKey.SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL, false)

    val disableListenerOnLowBattery: Flow<Boolean> =
        getFlow(PreferencesKey.DISABLE_LISTENER_ON_LOW_BATTERY, false)

    val manageExternalStoragePermissionPreviouslyGranted: Flow<Boolean> = getFlow(
        PreferencesKey.MANAGE_EXTERNAL_STORAGE_PERMISSION_PREVIOUSLY_GRANTED,
        !manageExternalStoragePermissionRequired()
    )

    val inAppTheme: Flow<Theme> = getEnumFLow(
        PreferencesKey.IN_APP_THEME,
        Theme.DeviceDefault
    )
}