package com.w2sv.filenavigator.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.w2sv.domain.usecase.PreviewMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.LocalMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.navigation.LocalNavigator
import com.w2sv.filenavigator.ui.navigation.PreviewNavigator
import com.w2sv.filenavigator.ui.theme.AppTheme

@Composable
fun PreviewOf(content: @Composable () -> Unit) {
    check(LocalInspectionMode.current) { "Calling preview composable outside of preview" }

    AppTheme {
        CompositionLocalProvider(
            LocalNavigator provides PreviewNavigator(),
            LocalMoveDestinationPathConverter provides PreviewMoveDestinationPathConverter(),
            content = content
        )
    }
}
