package com.w2sv.filenavigator.ui.shared.debugging

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import com.w2sv.filenavigator.ui.shared.LocalMoveDestinationLabelProvider

@Composable
fun PreviewOf(
    useDarkTheme: Boolean = false,
    useAmoledBlackTheme: Boolean = false,
    useDynamicColors: Boolean = false,
    content: @Composable () -> Unit
) {
    check(LocalInspectionMode.current) { "Calling preview composable outside of preview" }

    AppTheme(
        useDarkTheme = useDarkTheme,
        useAmoledBlackTheme = useAmoledBlackTheme,
        useDynamicColors = useDynamicColors
    ) {
        CompositionLocalProvider(
            LocalMoveDestinationLabelProvider provides PreviewMoveDestinationLabelProvider(),
            content = content
        )
    }
}

private class PreviewMoveDestinationLabelProvider : MoveDestinationLabelProvider {
    override operator fun invoke(moveDestination: MoveDestinationApi, context: Context): String =
        moveDestination.documentUri.uri.toString()
}
