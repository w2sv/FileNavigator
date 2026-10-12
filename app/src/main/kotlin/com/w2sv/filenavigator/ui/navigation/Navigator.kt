package com.w2sv.filenavigator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.navigation3.runtime.NavBackStack
import com.w2sv.composed.navigation3.Nav3Navigator
import com.w2sv.composed.navigation3.rememberTypedNavBackStack
import com.w2sv.core.logging.logIdentifier
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavRoute
import com.w2sv.filenavigator.ui.navigation.main.MainNavRoute
import slimber.log.i

@Stable
class Navigator(backStack: NavBackStack<MainNavRoute>) : Nav3Navigator<MainNavRoute>(backStack) {

    private val currentBottomNavHost: MainNavRoute.BottomNavHost?
        get() = currentRoute as? MainNavRoute.BottomNavHost

    val currentBottomNavRoute: BottomNavRoute?
        get() = currentBottomNavHost?.backStack?.lastOrNull()

    fun toAppSettings() =
        toBottomNavDestination(BottomNavRoute.AppSettings)

    fun toPermissions() {
        clearAndLaunch(MainNavRoute.Permissions)
    }

    fun toHome() =
        toBottomNavDestination(BottomNavRoute.Home)

    fun toNavigatorSettings() =
        toBottomNavDestination(BottomNavRoute.NavigatorSettings)

    private fun toBottomNavDestination(destination: BottomNavRoute) {
        if (currentBottomNavRoute == destination) return

        val host = currentBottomNavHost ?: MainNavRoute.BottomNavHost().also { clearAndLaunch(it) }
        if (destination == BottomNavRoute.Home) {
            host.backStack.resetTo(BottomNavRoute.Home)
        } else {
            host.backStack.resetTo(BottomNavRoute.Home, destination)
        }
    }
}

@Composable
fun rememberNavigator(startRoute: MainNavRoute): Navigator {
    val backStack = rememberTypedNavBackStack<MainNavRoute>(startRoute)

    LaunchedEffect(backStack) {
        snapshotFlow {
            backStack.map { route ->
                route to (route as? MainNavRoute.BottomNavHost)?.backStack?.toList()
            }
        }
            .collect { stack ->
                i { "BackStack=${stack.map { (route, nested) -> route.logIdentifier to nested?.map { it.logIdentifier } }}" }
            }
    }

    return remember(backStack) { Navigator(backStack) }
}
