package com.w2sv.filenavigator.ui.navigation.bottom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.w2sv.composed.ui.thenIfNotNull
import com.w2sv.designsystem.snackbar.AppSnackbarHost
import com.w2sv.filenavigator.ui.navigation.Navigator

class BottomNavScaffoldState {
    var fab: (@Composable () -> Unit)? by mutableStateOf<(@Composable () -> Unit)?>(null)
}

@Composable
fun BottomNavScaffold(navigator: Navigator, bottomNavScaffoldState: BottomNavScaffoldState, content: @Composable (PaddingValues) -> Unit) {
    val currentRoute = navigator.currentBottomNavRoute

    Scaffold(
        bottomBar = { BottomNavigationBar(navigator) },
        floatingActionButton = {
            AnimatedVisibility(currentRoute == NavigatorSettings && bottomNavScaffoldState.fab != null) {
                bottomNavScaffoldState.fab?.invoke()
            }
        },
        snackbarHost = { AppSnackbarHost() },
        contentWindowInsets = WindowInsets(),
        content = content
    )
}

@Composable
internal fun BottomNavDestinationLayout(
    route: BottomNavRoute,
    modifier: Modifier = Modifier,
    collapsible: Boolean = true,
    content: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior().takeIf { collapsible }

    Column(
        modifier = modifier
            .fillMaxSize()
            .thenIfNotNull(scrollBehavior) { nestedScroll(it.nestedScrollConnection) }
    ) {
        MediumTopAppBar(
            title = {
                Text(
                    text = stringResource(route.titleRes),
                    maxLines = 1
                )
            },
            scrollBehavior = scrollBehavior
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // For opaque background on predictive pop transition
        ) {
            content()
        }
    }
}
