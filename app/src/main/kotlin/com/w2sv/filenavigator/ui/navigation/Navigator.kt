package com.w2sv.filenavigator.ui.navigation

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import com.w2sv.composed.core.OnChange

interface Navigator {
    fun toAppSettings()
    fun toRequiredPermissions()
    fun leaveRequiredPermissions()
    fun toNavigatorSettings()
    fun popBackStack()
    val currentScreen: Screen
    val backStack: List<Screen>
}

@Stable
@VisibleForTesting
class NavigatorImpl(backStack: NavBackStack<Screen>) :
    Nav3Navigator<Screen>(backStack),
    Navigator {
    override fun toAppSettings() =
        launchSingleTop(Screen.AppSettings)

    override fun toRequiredPermissions() =
        clearAndLaunch(Screen.RequiredPermissions)

    override fun leaveRequiredPermissions() =
        clearAndLaunch(Screen.Home)

    override fun toNavigatorSettings() =
        launchSingleTop(Screen.NavigatorSettings)
}

@Composable
fun rememberNavigator(startScreen: Screen, allPermissionsGranted: () -> Boolean): Navigator {
    val backStack = rememberNavBackStack(startScreen)
    val navigator = remember(backStack) { NavigatorImpl(backStack) }

    OnChange(allPermissionsGranted()) {
        when {
            !it && navigator.currentScreen !is Screen.RequiredPermissions -> navigator.toRequiredPermissions()
            it && navigator.currentScreen is Screen.RequiredPermissions -> navigator.leaveRequiredPermissions()
        }
    }

    return navigator
}
