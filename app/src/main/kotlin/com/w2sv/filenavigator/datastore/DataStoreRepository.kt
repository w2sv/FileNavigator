package com.w2sv.filenavigator.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.filenavigator.mediastore.FileType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreRepository @Inject constructor(dataStore: DataStore<Preferences>) :
    AbstractDataStoreRepository(dataStore) {

    val accountForFileType: Map<FileType, Flow<Boolean>> = mapFromDataStoreProperties(FileType.all)

    val accountForFileTypeOrigin: Map<FileType.Origin, Flow<Boolean>> = mapFromDataStoreProperties(
        FileType.all
            .map { it.origins }
            .flatten()
    )

    val showedManageExternalStorageRational: Flow<Boolean> = dataStore.data.map {
        it[PreferencesKey.SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL] ?: false
    }
}