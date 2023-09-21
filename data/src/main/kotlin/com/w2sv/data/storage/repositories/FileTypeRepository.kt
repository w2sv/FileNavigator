package com.w2sv.data.storage.repositories

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.data.model.FileType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileTypeRepository @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore) {

    val fileTypeStatus: Map<FileType.Status.StoreEntry, Flow<FileType.Status>> =
        getEnumValuedFlowMap(FileType.values.map { it.status })

    val mediaFileSourceEnabled: Map<DataStoreEntry.UniType<Boolean>, Flow<Boolean>> =
        getFlowMap(
            FileType.Media.all
                .flatMap { it.sources }
                .map { it.isEnabled }
        )

    val defaultDestinationMap: Map<DataStoreEntry.UriValued, Flow<Uri?>> =
        getUriFlowMap(
            FileType.Media.all
                .flatMap { it.sources }
                .map { it.defaultDestination }
        )

    fun getDefaultDestinationFlow(source: FileType.Source): Flow<Uri?> =
        getUriFlow(source.defaultDestination)

    suspend fun saveDefaultDestination(
        source: FileType.Source,
        defaultDestination: Uri?
    ) {
        save(source.defaultDestination.preferencesKey, defaultDestination)
    }
}