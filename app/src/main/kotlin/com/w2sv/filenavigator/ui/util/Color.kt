package com.w2sv.filenavigator.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.w2sv.filenavigator.ui.theme.onSurfaceDisabled

@Composable
@ReadOnlyComposable
fun Color.orOnSurfaceDisabledIf(condition: Boolean): Color =
    if (condition) MaterialTheme.colorScheme.onSurfaceDisabled else this
