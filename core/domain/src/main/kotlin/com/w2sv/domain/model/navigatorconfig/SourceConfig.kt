package com.w2sv.domain.model.navigatorconfig

import android.net.Uri
import com.w2sv.domain.model.SourceType

data class SourceConfig(
    val type: SourceType,
    val enabled: Boolean,
    val lastMoveDestinations: List<Uri>,
    val autoMoveConfig: AutoMoveConfig
)