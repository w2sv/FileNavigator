package com.w2sv.domain.repository

import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import kotlinx.coroutines.flow.Flow

interface NavigatorConfigDataSource {
    val navigatorConfig: Flow<NavigatorConfig>
    suspend fun saveNavigatorConfig(config: NavigatorConfig)
}