package com.w2sv.datastorage.preferences.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.androidutils.datastorage.preferences_datastore.DataStoreEntry
import com.w2sv.androidutils.datastorage.preferences_datastore.PreferencesDataStoreRepository
import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreStateFlowMap
import com.w2sv.domain.model.FileType
import com.w2sv.domain.repository.NavigatorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
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

    override val fileTypeEnablementMap: DataStoreStateFlowMap<FileType, Boolean> =
        dataStoreFlowMap(FileType.values.associateWith { it.isEnabledDSE })
            .stateIn(
                scope,
                SharingStarted.Eagerly
            )

    override val mediaFileSourceEnablementMap: DataStoreStateFlowMap<FileType.Source, Boolean> =
        dataStoreFlowMap(
            FileType.Media.values
                .flatMap { it.sources }
                .associateWith { it.isEnabledDSE }
        )
            .stateIn(scope, SharingStarted.Eagerly)

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
    get() = DataStoreEntry.UniType.Impl(
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