package com.w2sv.domain.repository

import android.net.Uri
import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreFlow
import com.w2sv.androidutils.datastorage.preferences_datastore.flow.DataStoreStateFlowMap
import com.w2sv.domain.model.FileType
import kotlinx.coroutines.flow.Flow

interface NavigatorRepository {
    val disableOnLowBattery: DataStoreFlow<Boolean>

    val fileTypeEnablementMap: DataStoreStateFlowMap<FileType, Boolean>
    val mediaFileSourceEnablementMap: DataStoreStateFlowMap<FileType.Source, Boolean>

    fun getLastMoveDestinationFlow(source: FileType.Source): Flow<Uri?>
    suspend fun saveLastMoveDestination(source: FileType.Source, destination: Uri?)
}