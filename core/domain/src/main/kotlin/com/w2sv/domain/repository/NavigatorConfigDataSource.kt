package com.w2sv.domain.repository

import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import kotlinx.coroutines.flow.Flow

interface NavigatorConfigDataSource {
    val navigatorConfig: Flow<NavigatorConfig>
    suspend fun saveNavigatorConfig(config: NavigatorConfig)

    // ==================
    // Auto move
    // ==================

    suspend fun unsetAutoMoveConfig(fileType: FileType, sourceType: SourceType)

    // ==================
    // Quick move
    // ==================

    suspend fun saveQuickMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
        destination: LocalDestinationApi
    )

    suspend fun unsetQuickMoveDestination(fileType: FileType, sourceType: SourceType)

    fun quickMoveDestinations(fileType: FileType, sourceType: SourceType): Flow<List<LocalDestinationApi>>
}
