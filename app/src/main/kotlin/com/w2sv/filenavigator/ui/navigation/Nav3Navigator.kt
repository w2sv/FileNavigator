package com.w2sv.filenavigator.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Generic navigator methods/properties that aren't specific to the FileNavigator app navigation.
 */
abstract class Nav3Navigator(private val backStack: NavBackStack) {

    val currentScreen: NavKey
        get() = backStack.lastOrNull() ?: error("Back stack is empty")

    fun popBackStack() {
        backStack.removeLastOrNull()
    }

    protected fun launchSingleTop(target: NavKey) {
        if (backStack.lastOrNull() != target) {
            backStack.add(target)
        }
    }

    /**
     * Clears the [backStack] and launches [target].
     */
    protected fun clearAndLaunch(target: NavKey) {
        backStack.clear()
        backStack.add(target)
    }
}
