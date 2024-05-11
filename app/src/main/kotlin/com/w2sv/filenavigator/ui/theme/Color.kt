package com.w2sv.filenavigator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

object AppColor {
    val success = Color(12, 173, 34, 200)
    val error = Color(201, 14, 52, 200)

    val disabled: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface.copy(0.38f)
}