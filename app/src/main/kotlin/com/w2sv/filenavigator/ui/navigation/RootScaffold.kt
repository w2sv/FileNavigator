package com.w2sv.filenavigator.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarHost
import com.w2sv.filenavigator.ui.designsystem.TopAppBar

class RootScaffoldState {
    var fab: (@Composable () -> Unit)? by mutableStateOf<(@Composable () -> Unit)?>(null)
}

@Composable
fun rememberRootScaffoldState(): RootScaffoldState =
    remember { RootScaffoldState() }

@Composable
fun RootScaffold(navigator: Navigator, rootScaffoldState: RootScaffoldState, content: @Composable (PaddingValues) -> Unit) {
    val currentScreen = navigator.currentScreen

    Scaffold(
        topBar = { TopAppBar(title = stringResource(currentScreen.titleRes)) },
        bottomBar = {
            if (!currentScreen.isPermissions) {
                BottomNavigationBar(navigator)
            }
        },
        floatingActionButton = {
            AnimatedVisibility(currentScreen.isNavigatorSettings && rootScaffoldState.fab != null) {
                rootScaffoldState.fab?.invoke()
            }
        },
        snackbarHost = { AppSnackbarHost() },
        contentWindowInsets = WindowInsets(),
        content = content
    )
}
