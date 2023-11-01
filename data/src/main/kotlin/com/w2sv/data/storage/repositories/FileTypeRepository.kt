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
class FileTypeRepository @Inject constructor(
    dataStore: DataStore<Preferences>,
) :
    PreferencesDataStoreRepository(dataStore) {

    val fileTypeStatus: Map<DataStoreEntry.EnumValued<FileType.Status>, Flow<FileType.Status>> =
        getEnumValuedFlowMap(FileType.getValues().map { it.statusDSE })

    val mediaFileSourceEnabled: Map<DataStoreEntry.UniType<Boolean>, Flow<Boolean>> =
        getFlowMap(
            FileType.Media.getValues()
                .flatMap { it.sources }
                .map { it.isEnabledDSE }
        )

    // =======================
    // Last move destination
    // =======================

    fun getLastMoveDestinationFlow(source: FileType.Source): Flow<Uri?> =
        getUriFlow(source.lastMoveDestinationDSE)

    suspend fun saveLastMoveDestination(
        source: FileType.Source,
        destination: Uri?
    ) {
        save(source.lastMoveDestinationDSE.preferencesKey, destination)
    }
}