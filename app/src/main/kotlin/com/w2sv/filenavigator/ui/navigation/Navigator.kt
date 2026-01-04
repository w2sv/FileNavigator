package com.w2sv.filenavigator.ui.navigation

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack

interface Navigator {
    fun toAppSettings()
    fun toPermissions()
    fun toHome()
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

    override fun toPermissions() =
        clearAndLaunch(Screen.Permissions)

    override fun toHome() =
        clearAndLaunch(Screen.Home)

    override fun toNavigatorSettings() =
        launchSingleTop(Screen.NavigatorSettings)
}

@Composable
fun rememberNavigator(startScreen: Screen): Navigator {
    val backStack = rememberNavBackStack(startScreen)
    return remember(backStack) { NavigatorImpl(backStack) }
}
