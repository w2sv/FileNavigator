package com.w2sv.filenavigator.ui.navigation.bottom

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.w2sv.filenavigator.ui.navigation.NavTransitionDefaults
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.navigation.RoundedOnExit
import com.w2sv.filenavigator.ui.navigation.main.MainNavRoute
import com.w2sv.filenavigator.ui.screen.appsettings.AppSettingsScreenRoute
import com.w2sv.filenavigator.ui.screen.home.HomeScreenRoute
import com.w2sv.filenavigator.ui.screen.navigatorsettings.NavigatorSettingsScreenRoute

@Composable
internal fun BottomNavGraph(host: MainNavRoute.BottomNavHost, navigator: Navigator) {
    val scaffoldState = remember { BottomNavScaffoldState() }
    val fadeTransition =
        fadeIn(animationSpec = tween(NavTransitionDefaults.DurationMillis)) togetherWith
            fadeOut(animationSpec = tween(NavTransitionDefaults.DurationMillis))

    BottomNavScaffold(
        navigator = navigator,
        bottomNavScaffoldState = scaffoldState
    ) { paddingValues ->
        NavDisplay(
            backStack = host.backStack,
            onBack = navigator::popBackStack,
            modifier = Modifier.padding(paddingValues),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = { fadeTransition },
            popTransitionSpec = { fadeTransition },
            entryProvider = entryProvider {
                entry<BottomNavRoute.Home> {
                    RoundedOnExit {
                        BottomNavDestinationLayout(
                            route = BottomNavRoute.Home,
                            collapsible = false
                        ) {
                            HomeScreenRoute()
                        }
                    }
                }
                entry<BottomNavRoute.AppSettings> {
                    RoundedOnExit {
                        BottomNavDestinationLayout(BottomNavRoute.AppSettings) {
                            AppSettingsScreenRoute()
                        }
                    }
                }
                entry<BottomNavRoute.NavigatorSettings> {
                    RoundedOnExit {
                        BottomNavDestinationLayout(BottomNavRoute.NavigatorSettings) {
                            NavigatorSettingsScreenRoute(bottomNavScaffoldState = scaffoldState)
                        }
                    }
                }
            }
        )
    }
}
