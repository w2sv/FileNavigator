package com.w2sv.filenavigator.ui.screenshot

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavDestinationLayout
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavRoute
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavScaffold
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavScaffoldState
import com.w2sv.filenavigator.ui.navigation.main.MainNavRoute
import com.w2sv.filenavigator.ui.navigation.rememberNavigator
import com.w2sv.filenavigator.ui.screen.appsettings.AppSettingsScreen
import com.w2sv.filenavigator.ui.screen.home.HomeScreen
import com.w2sv.filenavigator.ui.screen.home.movehistory.MoveHistoryState
import com.w2sv.filenavigator.ui.screen.navigatorsettings.NavigatorSettingsScreen
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.PreviewNavigatorConfigActions
import com.w2sv.filenavigator.ui.shared.LocalMoveDestinationLabelProvider
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
    val route = screenshot.route
    val nestedBackStack = NavBackStack<BottomNavRoute>(BottomNavRoute.Home).apply {
        if (route != BottomNavRoute.Home) add(route)
    }
    val navigator = rememberNavigator(startRoute = MainNavRoute.BottomNavHost(nestedBackStack))

    BottomNavScaffold(
        navigator = navigator,
        bottomNavScaffoldState = BottomNavScaffoldState()
    ) { paddingValues ->
        BottomNavDestinationLayout(
            route = route,
            modifier = Modifier.padding(paddingValues),
            collapsible = route != BottomNavRoute.Home
        ) {
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

                StoreScreenshot.APP_SETTINGS -> AppSettingsScreen(
                    appSettings = fixture.appSettings,
                    updateAppSettings = {}
                )
            }
        }
    }
}
