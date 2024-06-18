package com.w2sv.navigator.fileobservers

import com.w2sv.common.utils.DocumentUri
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.navigator.moving.MoveMode

internal val AutoMoveConfig.moveMode: MoveMode?
    get() = enabledDestination
        ?.let { MoveMode.Auto(it) }

private val AutoMoveConfig.enabledDestination: DocumentUri?
    get() = if (enabled) destination else null