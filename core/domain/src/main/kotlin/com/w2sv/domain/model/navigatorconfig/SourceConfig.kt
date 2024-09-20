package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.MoveDestination

data class SourceConfig(
    val enabled: Boolean = true,
    val lastMoveDestinations: List<MoveDestination.Directory> = emptyList(),
    val autoMoveConfig: AutoMoveConfig = AutoMoveConfig.Empty
)