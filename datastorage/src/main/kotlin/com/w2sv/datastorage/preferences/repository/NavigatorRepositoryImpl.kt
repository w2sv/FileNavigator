package com.w2sv.datastorage.preferences.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.DataStoreEntry
import com.w2sv.androidutils.datastorage.datastore.DataStoreRepository
import com.w2sv.domain.model.FileType
import com.w2sv.domain.repository.NavigatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorRepositoryImpl @Inject constructor(dataStore: DataStore<Preferences>) :
    DataStoreRepository(dataStore),
    NavigatorRepository {

    override val disableOnLowBattery = dataStoreFlow(
        booleanPreferencesKey("disableNavigatorOnLowBattery"),
        true
    )

    override fun getFileTypeEnablementMap(): Map<FileType, Flow<Boolean>> {
        val dseToFileType = FileType.getValues()
            .associateBy { it.isEnabledDSE }
        return getFlowMap(dseToFileType.keys)
            .mapKeys { (k, _) -> dseToFileType.getValue(k) }
    }

    override suspend fun saveFileTypeEnablementMap(map: Map<FileType, Boolean>) {
        saveMap(map.mapKeys { (k, _) -> k.isEnabledDSE })
    }

    override fun getMediaFileSourceEnablementMap(): Map<FileType.Source, Flow<Boolean>> {
        val dseToSource = FileType.Media.getValues()
            .flatMap { it.sources }
            .associateBy { it.isEnabledDSE }
        return getFlowMap(dseToSource.keys)
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