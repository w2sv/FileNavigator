package com.w2sv.filenavigator.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.filenavigator.mediastore.FileType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreRepository @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore) {

    val accountForFileType: Map<FileType, Flow<Boolean>> = getFlowMap(FileType.all)

    val accountForFileTypeOrigin: Map<FileType.Origin, Flow<Boolean>> = getFlowMap(
        FileType.all
            .map { it.origins }
            .flatten()
    )

    val showedManageExternalStorageRational: Flow<Boolean> =
        getFlow(PreferencesKey.SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL, false)

    val disableListenerOnLowBattery: Flow<Boolean> =
        getFlow(PreferencesKey.DISABLE_LISTENER_ON_LOW_BATTERY, false)
}