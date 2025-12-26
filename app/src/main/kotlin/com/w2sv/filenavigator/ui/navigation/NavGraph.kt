package com.w2sv.filenavigator.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.w2sv.filenavigator.ui.screen.appsettings.AppSettingsScreen
import com.w2sv.filenavigator.ui.screen.home.HomeScreen
import com.w2sv.filenavigator.ui.screen.missingpermissions.RequiredPermissionsScreen
import com.w2sv.filenavigator.ui.screen.navigatorsettings.NavigatorSettingsScreen

@Composable
fun NavGraph(navigator: Navigator) {
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.popBackStack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            ContentTransform(
                NavAnimation.NonPop.enter(),
                NavAnimation.NonPop.exit()
            )
        },
        popTransitionSpec = {
            ContentTransform(
                NavAnimation.Pop.enter(),
                NavAnimation.Pop.exit()
            )
        },
        entryProvider = entryProvider {
            entry<Screen.Home> {
                HomeScreen()
            }
            entry<Screen.AppSettings> {
                AppSettingsScreen()
            }
            entry<Screen.RequiredPermissions> {
                RequiredPermissionsScreen()
            }
            entry<Screen.NavigatorSettings> {
                NavigatorSettingsScreen()
            }
        }
    )
}
