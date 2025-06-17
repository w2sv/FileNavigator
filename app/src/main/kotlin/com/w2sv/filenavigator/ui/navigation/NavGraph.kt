package com.w2sv.filenavigator.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.w2sv.composed.OnChange
import com.w2sv.filenavigator.ui.screen.appsettings.AppSettingsScreen
import com.w2sv.filenavigator.ui.screen.home.HomeScreen
import com.w2sv.filenavigator.ui.screen.missingpermissions.RequiredPermissionsScreen
import com.w2sv.filenavigator.ui.screen.navigatorsettings.NavigatorSettingsScreen

@Composable
fun NavGraph(anyPermissionMissing: Boolean) {
    val backStack = rememberNavBackStack(if (anyPermissionMissing) Screen.RequiredPermissions else Screen.Home)
    val navigator = remember(backStack) { Navigator(backStack) }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            transitionSpec = {
                ContentTransform(
                    slideInHorizontally(
                        initialOffsetX = { it }, // from right
                        animationSpec = springSpec
                    ),
                    slideOutHorizontally(
                        targetOffsetX = { -it }, // to left
                        animationSpec = springSpec
                    )
                )
            },
            popTransitionSpec = {
                ContentTransform(
                    slideInHorizontally(
                        initialOffsetX = { -it }, // from left (back)
                        animationSpec = springSpec
                    ),
                    slideOutHorizontally(
                        targetOffsetX = { it }, // to right (back)
                        animationSpec = springSpec
                    )
                )
            },
            entryProvider = entryProvider {
                entry<Screen.Home> {
                    HomeScreen()
                }
                entry<Screen.AppSettings> {
                    AppSettingsScreen()
                }
                entry<Screen.RequiredPermissions> {
                    RequiredPermissionsScreen()
                }
                entry<Screen.NavigatorSettings> {
                    NavigatorSettingsScreen()
                }
            }
        )

        OnChange(anyPermissionMissing) {
            if (it && navigator.currentScreen !is Screen.RequiredPermissions) {
                navigator.toRequiredPermissions()
            }
        }
    }
}

private val springSpec = spring<IntOffset>(
    stiffness = Spring.StiffnessMediumLow,
    dampingRatio = Spring.DampingRatioNoBouncy
)
