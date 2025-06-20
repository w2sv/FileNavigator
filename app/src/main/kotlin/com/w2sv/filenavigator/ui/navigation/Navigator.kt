package com.w2sv.filenavigator.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.w2sv.filenavigator.ui.noCompositionLocalProvidedFor

interface Navigator {
    fun toAppSettings()
    fun toRequiredPermissions()
    fun leaveRequiredPermissions()
    fun toNavigatorSettings()
    fun popBackStack()
    val currentScreen: NavKey
}

class NavigatorImpl(backStack: NavBackStack) : Navigator, Nav3Navigator(backStack) {
    override fun toAppSettings() = launchSingleTop(Screen.AppSettings)
    override fun toRequiredPermissions() = launchSingleTop(Screen.RequiredPermissions)

    override fun leaveRequiredPermissions() {
        popBackStack()
        if (backStack.isEmpty()) {
            backStack.add(Screen.Home)
        }
    }

    override fun toNavigatorSettings() = launchSingleTop(Screen.NavigatorSettings)
}

class PreviewNavigator : Navigator {
    override fun toAppSettings() {}
    override fun toRequiredPermissions() {}
    override fun leaveRequiredPermissions() {}
    override fun toNavigatorSettings() {}
    override fun popBackStack() {}
    override val currentScreen: NavKey = Screen.Home
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    noCompositionLocalProvidedFor("LocalNavigator")
}
