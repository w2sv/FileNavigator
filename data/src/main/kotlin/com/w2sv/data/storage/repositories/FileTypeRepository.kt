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
            FileType.Media.values
                .flatMap { it.sources }
                .map { it.isEnabledDSE }
        )

    // =======================
    // Default Destination
    // =======================

    val defaultDestinationMap: Map<DataStoreEntry.UriValued, Flow<Uri?>> =
        getUriFlowMap(
            FileType.values
                .flatMap { it.sources }
                .map { it.defaultDestinationDSE }
        )

    fun getDefaultDestinationFlow(source: FileType.Source): Flow<Uri?> =
        getUriFlow(source.defaultDestinationDSE)

    suspend fun saveDefaultDestination(
        source: FileType.Source,
        defaultDestination: Uri?
    ) {
        save(source.defaultDestinationDSE.preferencesKey, defaultDestination)
    }

    // =======================
    // Last manual move destination
    // =======================

    val lastManualMoveDestinationMap: Map<DataStoreEntry.UriValued, Flow<Uri?>> =
        getUriFlowMap(
            FileType.values
                .flatMap { it.sources }
                .map { it.lastManualMoveDestinationDSE }
        )

    fun getLastManualMoveDestinationFlow(source: FileType.Source): Flow<Uri?> =
        getUriFlow(source.lastManualMoveDestinationDSE)

    suspend fun saveLastManualMoveDestination(
        source: FileType.Source,
        destination: Uri?
    ) {
        save(source.lastManualMoveDestinationDSE.preferencesKey, destination)
    }
}