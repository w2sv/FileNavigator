package com.w2sv.filenavigator.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import com.w2sv.filenavigator.ui.noCompositionLocalProvidedFor

class Navigator(private val backStack: androidx.navigation3.runtime.NavBackStack) {
    fun toAppSettings() = backStack.add(Screen.AppSettings)
    fun toRequiredPermissions() = backStack.add(Screen.RequiredPermissions)
    fun leaveRequiredPermissions() {
        popBackStack()
        if (backStack.isEmpty()) {
            backStack.add(Screen.Home)
        }
    }

    fun toNavigatorSettings() = backStack.add(Screen.NavigatorSettings)
    fun popBackStack() = backStack.removeLastOrNull()

    val currentScreen: androidx.navigation3.runtime.NavKey
        get() = backStack.last()
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    noCompositionLocalProvidedFor("LocalNavigator")
}
