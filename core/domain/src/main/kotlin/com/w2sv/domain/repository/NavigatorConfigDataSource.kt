package com.w2sv.domain.repository

import android.net.Uri
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import kotlinx.coroutines.flow.Flow

interface NavigatorConfigDataSource {
    val navigatorConfig: Flow<NavigatorConfig>
    suspend fun saveNavigatorConfig(config: NavigatorConfig)

    suspend fun saveLastMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
        destination: Uri
    )

    fun lastMoveDestination(
        fileType: FileType,
        sourceType: SourceType
    ): Flow<List<Uri>>
}