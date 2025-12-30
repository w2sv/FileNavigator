package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.isPortraitModeActive

object Padding {
    val defaultHorizontal: Dp
        @Composable
        get() = if (isPortraitModeActive) 16.dp else 52.dp
}
