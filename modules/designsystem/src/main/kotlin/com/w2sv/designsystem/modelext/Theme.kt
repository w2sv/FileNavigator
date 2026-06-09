package com.w2sv.designsystem.modelext

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.w2sv.domain.model.settings.Theme

@Composable
@ReadOnlyComposable
fun useDarkTheme(theme: Theme): Boolean =
    when (theme) {
        Theme.Light -> false
        Theme.Dark -> true
        Theme.Default -> isSystemInDarkTheme()
    }
