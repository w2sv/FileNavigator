package com.w2sv.datastore.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.androidutils.datastorage.preferences_datastore.DataStoreEntry
import com.w2sv.androidutils.datastorage.preferences_datastore.PreferencesDataStoreRepository
import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreStateFlowMap
import com.w2sv.domain.model.FileTypeKind
import com.w2sv.domain.repository.NavigatorConfigDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorConfigDataSourceImpl @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore),
    NavigatorConfigDataSource {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val disableOnLowBattery = dataStoreFlow(
        booleanPreferencesKey("disableNavigatorOnLowBattery"),
        true
    )

    override val fileTypeEnablementMap: DataStoreStateFlowMap<FileTypeKind, Boolean> =
        dataStoreFlowMap(FileTypeKind.values.associateWith { it.isEnabledDSE })
            .stateIn(
                scope,
                SharingStarted.Eagerly
            )

    override val mediaFileSourceEnablementMap: DataStoreStateFlowMap<FileTypeKind.Source, Boolean> =
        dataStoreFlowMap(
            FileTypeKind.Media.values
                .flatMap { it.sources }
                .associateWith { it.isEnabledDSE }
        )
            .stateIn(scope, SharingStarted.Eagerly)

    // =======================
    // Last move destination
    // =======================

    override fun getLastMoveDestinationFlow(source: FileTypeKind.Source): Flow<Uri?> =
        getUriFlow(source.lastMoveDestinationDSE)

    override suspend fun saveLastMoveDestination(
        source: FileTypeKind.Source,
        destination: Uri?
    ) {
        saveStringRepresentation(source.lastMoveDestinationDSE.preferencesKey, destination)
    }
}

private val FileTypeKind.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(name = name),
        defaultValue = true
    )

private val FileTypeKind.Source.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        booleanPreferencesKey(getPreferencesKeyContent("IS_ENABLED")),
        true
    )

private val FileTypeKind.Source.lastMoveDestinationDSE
    get() = DataStoreEntry.UriValued.Impl(
        stringPreferencesKey(getPreferencesKeyContent("LAST_MOVE_DESTINATION")),
        null
    )

private fun FileTypeKind.Source.getPreferencesKeyContent(keySuffix: String): String =
    "${fileType.name}.$kind.$keySuffix"