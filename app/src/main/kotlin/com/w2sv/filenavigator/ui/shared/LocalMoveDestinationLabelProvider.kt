package com.w2sv.filenavigator.ui.shared

import androidx.compose.runtime.staticCompositionLocalOf
import com.w2sv.domain.usecase.MoveDestinationLabelProvider

val LocalMoveDestinationLabelProvider = staticCompositionLocalOf<MoveDestinationLabelProvider> {
    error("LocalMoveDestinationLabelProvider not provided")
}
