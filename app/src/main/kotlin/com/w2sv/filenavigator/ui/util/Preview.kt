package com.w2sv.filenavigator.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.w2sv.domain.model.movedestination.ExternalDestinationApi
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.filenavigator.ui.LocalMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.LocalNavigator
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.navigation.Screen
import com.w2sv.filenavigator.ui.theme.AppTheme

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
            LocalNavigator provides PreviewNavigator(),
            LocalMoveDestinationPathConverter provides PreviewMoveDestinationPathConverter(),
            content = content
        )
    }
}

private class PreviewNavigator : Navigator {
    override fun toAppSettings() {}
    override fun toRequiredPermissions() {}
    override fun leaveRequiredPermissions() {}
    override fun toNavigatorSettings() {}
    override fun popBackStack() {}
    override val currentScreen = Screen.Home
    override val backStack = emptyList<Screen>()
}

private class PreviewMoveDestinationPathConverter : MoveDestinationPathConverter {
    override operator fun invoke(moveDestination: MoveDestinationApi, context: Context): String =
        when (moveDestination) {
            is LocalDestinationApi -> {
                moveDestination.pathRepresentation(
                    context = context,
                    includeVolumeName = true
                )
            }

            is ExternalDestinationApi -> {
                moveDestination.uiRepresentation(
                    context
                )
            }

            else -> error("")
        }
}
