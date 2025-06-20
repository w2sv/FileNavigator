package com.w2sv.filenavigator.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

abstract class Nav3Navigator(protected val backStack: NavBackStack) {

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
}
