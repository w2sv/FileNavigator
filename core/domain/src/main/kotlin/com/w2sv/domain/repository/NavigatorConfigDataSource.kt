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

    suspend fun saveQuickMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
        destination: MoveDestination
    )

    suspend fun unsetQuickMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
    )

    fun quickMoveDestinations(
        fileType: FileType,
        sourceType: SourceType
    ): Flow<List<MoveDestination>>
}