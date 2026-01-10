package com.w2sv.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

object ResultColor {
    val success = Color(12, 173, 34, 200)
    val error = Color(201, 14, 52, 200)
}

val ColorScheme.onSurfaceDisabled: Color
    @Composable
    @ReadOnlyComposable
    get() = onSurface.copy(0.38f)

val ColorScheme.onSurfaceVariantDecreasedAlpha: Color
    @Composable
    @ReadOnlyComposable
    get() = onSurfaceVariant.copy(0.6f)

@Composable
@ReadOnlyComposable
fun Color.orOnSurfaceDisabledIf(condition: Boolean): Color =
    if (condition) colorScheme.onSurfaceDisabled else this
