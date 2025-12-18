package com.w2sv.filenavigator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    noCompositionLocalProvidedFor("LocalNavigator")
}

class PreviewNavigator : Navigator {
    override fun toAppSettings() {}
    override fun toRequiredPermissions() {}
    override fun leaveRequiredPermissions() {}
    override fun toNavigatorSettings() {}
    override fun popBackStack() {}
    override val currentScreen: NavKey = Screen.Home
}

@Composable
fun WithNavigatorMock(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavigator provides PreviewNavigator(), content)
}
