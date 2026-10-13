package com.w2sv.filenavigator.ui.screenshot

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.w2sv.filenavigator.ui.FileNavigatorUiEnvironment
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavDestinationLayout
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavScaffold
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavScaffoldState
import com.w2sv.filenavigator.ui.screen.appsettings.AppSettingsScreen
import com.w2sv.filenavigator.ui.screen.home.HomeScreen
import com.w2sv.filenavigator.ui.screen.home.movehistory.MoveHistoryState
import com.w2sv.filenavigator.ui.screen.navigatorsettings.NavigatorSettingsScreen
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.PreviewNavigatorConfigActions
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun StoreScreenshotTheme(fixture: StoreScreenshotFixture, content: @Composable () -> Unit) {
    FileNavigatorUiEnvironment(
        themeSettings = fixture.appSettings.theme,
        moveDestinationLabelProvider = fixture.destinationLabelProvider,
        content = content
    )
}

@Composable
internal fun StoreScreenshotContent(screenshot: StoreScreenshot, fixture: StoreScreenshotFixture) {
    val route = screenshot.route
    BottomNavScaffold(
        bottomNavScaffoldState = BottomNavScaffoldState(route),
        onNavigate = {}
    ) { paddingValues ->
        BottomNavDestinationLayout(
            route = route,
            modifier = Modifier.padding(paddingValues)
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
