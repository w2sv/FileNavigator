package com.w2sv.filenavigator.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.w2sv.filenavigator.ui.navigation.LocalNavigator
import com.w2sv.filenavigator.ui.navigation.PreviewNavigator
import com.w2sv.filenavigator.ui.theme.AppTheme

@Composable
fun PreviewOf(content: @Composable () -> Unit) {
    AppTheme {
        CompositionLocalProvider(LocalNavigator provides PreviewNavigator(), content)
    }
}
