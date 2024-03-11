package com.w2sv.domain.repository

import android.net.Uri
import com.w2sv.androidutils.datastorage.preferences_datastore.DataStoreFlow
import com.w2sv.domain.model.FileType
import kotlinx.coroutines.flow.Flow

interface NavigatorRepository {
    val disableOnLowBattery: DataStoreFlow<Boolean>

    fun getFileTypeEnablementMap(): Map<FileType, Flow<Boolean>>
    suspend fun saveFileTypeEnablementMap(map: Map<FileType, Boolean>)

    fun getMediaFileSourceEnablementMap(): Map<FileType.Source, Flow<Boolean>>
    suspend fun saveMediaFileSourceEnablementMap(map: Map<FileType.Source, Boolean>)

    fun getLastMoveDestinationFlow(source: FileType.Source): Flow<Uri?>
    suspend fun saveLastMoveDestination(source: FileType.Source, destination: Uri?)
}