package com.w2sv.filenavigator.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.w2sv.filenavigator.ui.screen.appsettings.AppSettingsScreenRoute
import com.w2sv.filenavigator.ui.screen.home.HomeScreenRoute
import com.w2sv.filenavigator.ui.screen.navigatorsettings.NavigatorSettingsScreenRoute
import com.w2sv.filenavigator.ui.screen.permissions.PermissionsScreenRoute
import com.w2sv.filenavigator.ui.shared.permissions.AppPermissionsState

@Composable
fun NavContent(
    navigator: Navigator,
    rootScaffoldState: RootScaffoldState,
    permissionsState: AppPermissionsState,
    modifier: Modifier = Modifier
) {
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.popBackStack() },
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            ContentTransform(
                NavAnimation.PrimaryDestination.enter(),
                NavAnimation.PrimaryDestination.exit()
            )
        },
        popTransitionSpec = {
            ContentTransform(
                NavAnimation.PrimaryDestination.enter(),
                NavAnimation.PrimaryDestination.exit()
            )
        },
        entryProvider = entryProvider {
            entry<Screen.Home> {
                HomeScreenRoute()
            }
            entry<Screen.AppSettings> {
                AppSettingsScreenRoute()
            }
            entry<Screen.Permissions> {
                PermissionsScreenRoute(
                    permissionsState = permissionsState,
                    onAllPermissionsGranted = navigator::toHome
                )
            }
            entry<Screen.NavigatorSettings> {
                NavigatorSettingsScreenRoute(rootScaffoldState = rootScaffoldState)
            }
        }
    )
}
