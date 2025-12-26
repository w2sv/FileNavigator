package com.w2sv.filenavigator.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Generic navigator methods/properties that aren't specific to the FileNavigator app navigation.
 */
abstract class Nav3Navigator<T : NavKey>(val backStack: NavBackStack<T>) {

    val currentScreen: T
        get() = backStack.lastOrNull() ?: error("Back stack is empty")

    fun popBackStack() {
        backStack.removeLastOrNull()
    }

    protected fun launchSingleTop(target: T) {
        if (backStack.lastOrNull() != target) {
            backStack.add(target)
        }
    }

    /**
     * Clears the [backStack] and launches [target].
     */
    protected fun clearAndLaunch(target: T) {
        backStack.clear()
        backStack.add(target)
    }
}
