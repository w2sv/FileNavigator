package com.w2sv.filenavigator.ui.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.w2sv.domain.model.Theme

@Composable
@ReadOnlyComposable
fun useDarkTheme(theme: Theme): Boolean =
    when (theme) {
        Theme.Light -> false
        Theme.Dark -> true
        Theme.Default -> isSystemInDarkTheme()
    }
