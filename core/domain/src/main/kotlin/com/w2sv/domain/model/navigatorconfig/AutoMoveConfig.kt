package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.movedestination.LocalDestinationApi

data class AutoMoveConfig(val enabled: Boolean, val destination: LocalDestinationApi?) {
    companion object {
        val Empty = AutoMoveConfig(enabled = false, destination = null)
    }
}
