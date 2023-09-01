package com.w2sv.filenavigator.data

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.filenavigator.domain.model.FileType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileTypeRepository @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore) {

    val fileTypeStatus: Map<FileType.Status.StoreEntry, Flow<FileType.Status>> =
        getEnumValuedFlowMap(FileType.values.map { it.status })

    val mediaFileSourceEnabled: Map<FileType.Source.IsEnabled, Flow<Boolean>> = getFlowMap(
        FileType.Media.all
            .map { it.sources }
            .flatten()
            .map { it.isEnabled }
    )

    fun getFileSourceDefaultDestinationFlow(source: FileType.Source): Flow<Uri?> =
        getUriFlow(source.defaultDestination)

    suspend fun saveFileSourceDefaultDestination(source: FileType.Source, defaultDestination: Uri?) {
        save(source.defaultDestination.preferencesKey, defaultDestination)
    }

    fun getFileSourceDefaultDestinationIsLockedFlow(source: FileType.Source): Flow<Boolean> =
        getFlow(source.defaultDestinationIsLocked)

    suspend fun saveFileSourceDefaultDestinationIsLocked(source: FileType.Source, isLocked: Boolean) {
        save(source.defaultDestinationIsLocked.preferencesKey, isLocked)
    }
}