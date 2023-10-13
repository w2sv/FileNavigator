package com.w2sv.data.storage.repositories

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.Scope
import com.w2sv.data.model.FileType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileTypeRepository @Inject constructor(
    dataStore: DataStore<Preferences>,
    @Scope(AppDispatcher.Default) scope: CoroutineScope
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
    // Default Destination
    // =======================

    val defaultDestinationStateFlowMap: Map<DataStoreEntry.UriValued, StateFlow<Uri?>> =
        getUriFlowMap(
            FileType.getValues()
                .flatMap { it.sources }
                .map { it.defaultDestinationDSE }
        )
            .mapValues { (_, v) -> v.stateIn(scope, SharingStarted.Eagerly, null) }

    fun getDefaultDestination(source: FileType.Source): Uri? =
        defaultDestinationStateFlowMap
            .getValue(source.defaultDestinationDSE).value

    suspend fun saveDefaultDestination(
        source: FileType.Source,
        defaultDestination: Uri?
    ) {
        save(source.defaultDestinationDSE.preferencesKey, defaultDestination)
    }

    // =======================
    // Last manual move destination
    // =======================

    private val lastManualMoveDestinationStateFlowMap: Map<DataStoreEntry.UriValued, StateFlow<Uri?>> =
        getUriFlowMap(
            FileType.getValues()
                .flatMap { it.sources }
                .map { it.lastManualMoveDestinationDSE }
        )
            .mapValues { (_, v) -> v.stateIn(scope, SharingStarted.Eagerly, null) }

    fun getLastManualMoveDestination(source: FileType.Source): Uri? =
        lastManualMoveDestinationStateFlowMap.getValue(source.lastManualMoveDestinationDSE).value

    suspend fun saveLastManualMoveDestination(
        source: FileType.Source,
        destination: Uri?
    ) {
        save(source.lastManualMoveDestinationDSE.preferencesKey, destination)
    }
}