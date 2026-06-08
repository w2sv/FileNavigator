package com.w2sv.filenavigator.ui.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.filenavigator.ui.LocalMoveDestinationLabelProvider
import com.w2sv.filenavigator.ui.navigation.RootScaffold
import com.w2sv.filenavigator.ui.navigation.Screen
import com.w2sv.filenavigator.ui.navigation.rememberNavigator
import com.w2sv.filenavigator.ui.navigation.rememberRootScaffoldState
import com.w2sv.filenavigator.ui.screen.appsettings.AppSettingsScreen
import com.w2sv.filenavigator.ui.screen.home.HomeScreen
import com.w2sv.filenavigator.ui.screen.home.movehistory.MoveHistoryState
import com.w2sv.filenavigator.ui.screen.navigatorsettings.NavigatorSettingsScreen
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.PreviewNavigatorConfigActions
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun StoreScreenshotTheme(fixture: StoreScreenshotFixture, content: @Composable () -> Unit) {
    AppTheme(
        useDarkTheme = false,
        useAmoledBlackTheme = false,
        useDynamicColors = false
    ) {
        CompositionLocalProvider(
            LocalMoveDestinationLabelProvider provides fixture.destinationLabelProvider,
            content = content
        )
    }
}

@Composable
internal fun StoreScreenshotContent(screenshot: StoreScreenshot, fixture: StoreScreenshotFixture) {
    val screen = when (screenshot) {
        StoreScreenshot.HOME -> Screen.Home
        StoreScreenshot.NAVIGATOR_SETTINGS -> Screen.NavigatorSettings
        StoreScreenshot.APP_SETTINGS -> Screen.AppSettings
    }
    val navigator = rememberNavigator(startScreen = screen)

    RootScaffold(
        navigator = navigator,
        rootScaffoldState = rememberRootScaffoldState()
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (screenshot) {
                StoreScreenshot.HOME -> HomeScreen(
                    navigatorIsRunning = true,
                    moveHistoryState = MoveHistoryState(
                        history = fixture.moveHistory.toImmutableList(),
                        deleteAll = {},
                        deleteEntry = {}
                    )
                )

                StoreScreenshot.NAVIGATOR_SETTINGS -> NavigatorSettingsScreen(
                    navigatorConfig = fixture.navigatorConfig,
                    navigatorConfigActions = PreviewNavigatorConfigActions,
                    showFileTypesBottomSheet = {},
                    showFileTypeConfigurationDialog = {}
                )

                StoreScreenshot.APP_SETTINGS -> AppSettingsScreen(appPreferences = fixture.appPreferences)
            }
        }
    }
}
