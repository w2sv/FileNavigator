package com.w2sv.filenavigator.ui.navigation

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.navigation3.runtime.NavBackStack
import com.w2sv.composed.navigation3.Nav3Navigator
import com.w2sv.composed.navigation3.rememberTypedNavBackStack
import com.w2sv.core.logging.logIdentifier
import slimber.log.i

interface Navigator {
    fun toAppSettings()
    fun toPermissions()
    fun toHome()
    fun toNavigatorSettings()
    fun popBackStack()
    val currentRoute: Screen
    val backStack: List<Screen>
}

@Stable
@VisibleForTesting
class NavigatorImpl(backStack: NavBackStack<Screen>) :
    Nav3Navigator<Screen>(backStack),
    Navigator {

    override fun toAppSettings() =
        toBottomNavDestination(Screen.AppSettings)

    override fun toPermissions() {
        clearAndLaunch(Screen.Permissions)
    }

    override fun toHome() =
        toBottomNavDestination(Screen.Home)

    override fun toNavigatorSettings() =
        toBottomNavDestination(Screen.NavigatorSettings)

    private fun toBottomNavDestination(destination: Screen) {
        val current = currentRoute
        when {
            current == destination -> Unit
            !current.isBottomNavDestination -> clearAndLaunch(destination)
            else -> {
                if (!current.isHome) {
                    popBackStack()
                }
                launchSingleTop(destination)
            }
        }
    }
}

@Composable
fun rememberNavigator(startScreen: Screen): Navigator {
    val backStack = rememberTypedNavBackStack<Screen>(startScreen)

    LaunchedEffect(backStack) {
        snapshotFlow { backStack.toList() }
            .collect { stack ->
                i { "BackStack=${stack.map { it.logIdentifier }}" }
            }
    }

    return remember(backStack) { NavigatorImpl(backStack) }
}
