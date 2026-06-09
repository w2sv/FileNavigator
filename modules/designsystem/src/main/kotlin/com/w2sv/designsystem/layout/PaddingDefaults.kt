package com.w2sv.designsystem.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w2sv.composed.core.isPortraitModeActive

object PaddingDefaults {
    val horizontal: Dp
        @ReadOnlyComposable
        @Composable
        get() = if (isPortraitModeActive) 16.dp else 52.dp
}
