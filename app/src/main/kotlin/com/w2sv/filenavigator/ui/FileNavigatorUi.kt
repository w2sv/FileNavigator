package com.w2sv.filenavigator.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.w2sv.designsystem.modelext.useDarkTheme
import com.w2sv.designsystem.theme.FileNavigatorTheme
import com.w2sv.domain.model.settings.ThemeSettings
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import com.w2sv.filenavigator.ui.navigation.main.MainNavGraph
import com.w2sv.filenavigator.ui.navigation.main.MainNavRoute
import com.w2sv.filenavigator.ui.navigation.rememberNavigator
import com.w2sv.filenavigator.ui.shared.LocalMoveDestinationLabelProvider
import com.w2sv.filenavigator.ui.shared.debugging.DebugAction
import com.w2sv.filenavigator.ui.shared.permissions.AppPermissionsState
import com.w2sv.navigator.FileNavigator

@Composable
internal fun FileNavigatorUi(
    themeSettings: ThemeSettings,
    startRoute: MainNavRoute,
    permissionsState: AppPermissionsState,
    moveDestinationLabelProvider: MoveDestinationLabelProvider,
    debugAction: DebugAction?
) {
    val navigator = rememberNavigator(startRoute)

    FileNavigatorUiEnvironment(themeSettings, moveDestinationLabelProvider) {
        MainNavGraph(navigator = navigator, permissionsState = permissionsState)
    }

    val context = LocalContext.current
    LaunchedEffect(debugAction) {
        when (debugAction) {
            DebugAction.LaunchNavigatorSettings -> navigator.toNavigatorSettings()
            DebugAction.StartNavigator -> FileNavigator.start(context)
            null -> Unit
        }
    }
}

@Composable
internal fun FileNavigatorUiEnvironment(
    themeSettings: ThemeSettings,
    moveDestinationLabelProvider: MoveDestinationLabelProvider,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalMoveDestinationLabelProvider provides moveDestinationLabelProvider) {
        FileNavigatorTheme(
            useDarkTheme = useDarkTheme(themeSettings.theme),
            useAmoledBlackTheme = themeSettings.useAmoledBlackTheme,
            useDynamicColors = themeSettings.useDynamicColors,
            content = content
        )
    }
}
