package com.w2sv.domain.repository

import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import kotlinx.coroutines.flow.Flow

typealias NavigatorConfigFlow = Flow<NavigatorConfig>

interface NavigatorConfigDataSource {
    val config: NavigatorConfigFlow

    suspend fun update(transform: suspend (NavigatorConfig) -> NavigatorConfig)
}
