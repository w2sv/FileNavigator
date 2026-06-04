package com.w2sv.filenavigator.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Devices.PHONE
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import com.w2sv.filenavigator.ui.LocalMoveDestinationLabelProvider

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

@Preview(name = "Phone", device = PHONE, showSystemUi = true)
@Preview(
    name = "Phone - Landscape",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    showSystemUi = true
)
@Preview(
    name = "Tablet",
    device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait",
    showSystemUi = true
)
@Preview(name = "Tablet - Landscape", device = TABLET, showSystemUi = true)
annotation class ScreenPreviews
