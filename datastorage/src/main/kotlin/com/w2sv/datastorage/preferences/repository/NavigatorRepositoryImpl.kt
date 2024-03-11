package com.w2sv.datastorage.preferences.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.androidutils.datastorage.preferences_datastore.DataStoreEntry
import com.w2sv.androidutils.datastorage.preferences_datastore.PreferencesDataStoreRepository
import com.w2sv.domain.model.FileType
import com.w2sv.domain.repository.NavigatorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorRepositoryImpl @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore),
    NavigatorRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val disableOnLowBattery = dataStoreFlow(
        booleanPreferencesKey("disableNavigatorOnLowBattery"),
        true
    )

    override val fileTypeEnablementMap: Map<FileType, StateFlow<Boolean>> =
        FileType.values
            .associateBy { it.isEnabledDSE }
            .let { dseToFileType ->
                getStateFlowMap(
                    properties = dseToFileType.keys,
                    scope = scope,
                    sharingStarted = SharingStarted.Eagerly
                )
                    .mapKeys { (k, _) -> dseToFileType.getValue(k) }
            }

    override suspend fun saveFileTypeEnablementMap(map: Map<FileType, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    override val mediaFileSourceEnablementMap: Map<FileType.Source, StateFlow<Boolean>> =
        FileType.Media.values
            .flatMap { it.sources }
            .associateBy { it.isEnabledDSE }
            .let { dseToSource ->
                getStateFlowMap(
                    properties = dseToSource.keys,
                    scope = scope,
                    sharingStarted = SharingStarted.Eagerly
                )
                    .mapKeys { (k, _) -> dseToSource.getValue(k) }
            }

    override suspend fun saveMediaFileSourceEnablementMap(map: Map<FileType.Source, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    // =======================
    // Last move destination
    // =======================

    override fun getLastMoveDestinationFlow(source: FileType.Source): Flow<Uri?> =
        getUriFlow(source.lastMoveDestinationDSE)

    override suspend fun saveLastMoveDestination(
        source: FileType.Source,
        destination: Uri?
    ) {
        saveStringRepresentation(source.lastMoveDestinationDSE.preferencesKey, destination)
    }
}

private val FileType.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(name = name),
        defaultValue = true
    )

private val FileType.Source.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(  // TODO: Remove for NonMedia
        booleanPreferencesKey(getPreferencesKeyContent("IS_ENABLED")),
        true
    )

private val FileType.Source.lastMoveDestinationDSE
    get() = DataStoreEntry.UriValued.Impl(
        stringPreferencesKey(getPreferencesKeyContent("LAST_MOVE_DESTINATION")),
        null
    )

private fun FileType.Source.getPreferencesKeyContent(keySuffix: String): String =
    "${fileType.name}.$kind.$keySuffix"