package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.movedestination.LocalDestinationApi

data class SourceConfig(
    val enabled: Boolean = true,
    val quickMoveDestinations: List<LocalDestinationApi> = emptyList(),
    val autoMoveConfig: AutoMoveConfig = AutoMoveConfig.Empty
)
