package com.w2sv.filenavigator.ui.navigation.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.w2sv.composed.runtime.CollectFromFlow
import com.w2sv.filenavigator.ui.navigation.NavTransitionDefaults
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.navigation.bottom.BottomNavGraph
import com.w2sv.filenavigator.ui.screen.permissions.PermissionsScreenRoute
import com.w2sv.filenavigator.ui.shared.permissions.AppPermissionsState

@Composable
fun MainNavGraph(navigator: Navigator, permissionsState: AppPermissionsState) {
    CollectFromFlow(permissionsState.allGranted) { allPermissionsGranted ->
        if (!allPermissionsGranted && navigator.currentRoute != MainNavRoute.Permissions) {
            navigator.toPermissions()
        }
    }

    NavDisplay(
        backStack = navigator.backStack,
        onBack = navigator::popBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally(animationSpec = tween(NavTransitionDefaults.DurationMillis), initialOffsetX = { it }) togetherWith
                slideOutHorizontally(animationSpec = tween(NavTransitionDefaults.DurationMillis), targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(animationSpec = tween(NavTransitionDefaults.DurationMillis), initialOffsetX = { -it }) togetherWith
                slideOutHorizontally(animationSpec = tween(NavTransitionDefaults.DurationMillis), targetOffsetX = { it })
        },
        entryProvider = entryProvider {
            entry<MainNavRoute.Permissions> {
                PermissionsScreenRoute(
                    permissionsState = permissionsState,
                    onAllPermissionsGranted = navigator::toHome
                )
            }
            entry<MainNavRoute.BottomNavHost>(clazzContentKey = { "bottom-nav-host" }) { host ->
                BottomNavGraph(host = host, navigator = navigator)
            }
        }
    )
}
