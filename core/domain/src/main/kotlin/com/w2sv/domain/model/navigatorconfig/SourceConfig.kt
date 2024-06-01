package com.w2sv.domain.model.navigatorconfig

import android.net.Uri

data class SourceConfig(
    val enabled: Boolean = true,
    val lastMoveDestinations: List<Uri> = emptyList(),
    val autoMoveConfig: AutoMoveConfig = AutoMoveConfig()
)