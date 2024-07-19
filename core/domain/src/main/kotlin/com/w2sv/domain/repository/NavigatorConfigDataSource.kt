package com.w2sv.domain.repository

import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import kotlinx.coroutines.flow.Flow

interface NavigatorConfigDataSource {
    val navigatorConfig: Flow<NavigatorConfig>
    suspend fun saveNavigatorConfig(config: NavigatorConfig)

    suspend fun unsetAutoMoveConfig(fileType: FileType, sourceType: SourceType)

    suspend fun saveLastMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
        destination: MoveDestination
    )

    suspend fun unsetLastMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
    )

    fun lastMoveDestination(
        fileType: FileType,
        sourceType: SourceType
    ): Flow<List<MoveDestination>>
}