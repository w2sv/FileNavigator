package com.w2sv.filenavigator.ui.util

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun WithLocalContentColor(color: Color, content: @Composable () -> Unit) {
    CompositionLocalProvider(value = LocalContentColor provides color, content = content)
}
