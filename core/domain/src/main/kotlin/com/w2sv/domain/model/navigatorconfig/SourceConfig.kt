package com.w2sv.domain.model.navigatorconfig

import com.w2sv.common.utils.DocumentUri

data class SourceConfig(
    val enabled: Boolean = true,
    val lastMoveDestinations: List<DocumentUri> = emptyList(),
    val autoMoveConfig: AutoMoveConfig = AutoMoveConfig.Empty
)