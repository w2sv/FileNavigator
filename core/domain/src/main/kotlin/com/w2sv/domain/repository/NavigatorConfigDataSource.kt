package com.w2sv.domain.repository

import android.net.Uri
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import kotlinx.coroutines.flow.Flow

interface NavigatorConfigDataSource {
    val navigatorConfig: Flow<NavigatorConfig>
    suspend fun saveNavigatorConfig(config: NavigatorConfig)

    suspend fun saveLastMoveDestination(
        fileAndSourceType: FileAndSourceType,
        destination: Uri
    )

    fun lastMoveDestinations(fileAndSourceType: FileAndSourceType): Flow<List<Uri>>
}