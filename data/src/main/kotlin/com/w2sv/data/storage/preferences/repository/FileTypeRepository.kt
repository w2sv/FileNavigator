package com.w2sv.data.storage.preferences.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.data.storage.preferences.model.isEnabledDSE
import com.w2sv.data.storage.preferences.model.lastMoveDestinationDSE
import com.w2sv.domain.model.FileType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileTypeRepository @Inject constructor(
    dataStore: DataStore<Preferences>
) :
    PreferencesDataStoreRepository(dataStore) {

    fun getFileTypeEnablementMap(): Map<FileType, Flow<Boolean>> {
        val dseToFileType = FileType.getValues()
            .associateBy { it.isEnabledDSE }
        return getFlowMap(dseToFileType.keys)
            .mapKeys { (k, _) -> dseToFileType.getValue(k) }
    }

    suspend fun saveFileTypeEnablementMap(map: Map<FileType, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    fun getMediaFileSourceEnablementMap(): Map<FileType.Source, Flow<Boolean>> {
        val dseToSource = FileType.Media.getValues()
            .flatMap { it.sources }
            .associateBy { it.isEnabledDSE }
        return getFlowMap(dseToSource.keys)
            .mapKeys { (k, _) -> dseToSource.getValue(k) }
    }


    suspend fun saveMediaFileSourceEnablementMap(map: Map<FileType.Source, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    // =======================
    // Last move destination
    // =======================

    fun getLastMoveDestinationFlow(source: FileType.Source): Flow<Uri?> =
        getUriFlow(source.lastMoveDestinationDSE)

    suspend fun saveLastMoveDestination(
        source: FileType.Source,
        destination: Uri?
    ) {
        saveStringRepresentation(source.lastMoveDestinationDSE.preferencesKey, destination)
    }
}