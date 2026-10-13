package com.w2sv.filenavigator.ui.navigation.bottom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.w2sv.designsystem.snackbar.AppSnackbarHost
import com.w2sv.filenavigator.ui.shared.debugging.PreviewOf
import com.w2sv.filenavigator.ui.shared.debugging.ScreenPreviews

@ScreenPreviews
@Composable
private fun BottomNavScaffoldPrev() {
    PreviewOf {
        BottomNavScaffold(
            bottomNavScaffoldState = remember { BottomNavScaffoldState(Home) },
            onNavigate = {}
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                content = {}
            )
        }
    }
}

@Composable
fun BottomNavScaffold(
    bottomNavScaffoldState: BottomNavScaffoldState,
    onNavigate: (BottomNavRoute) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val currentRoute = bottomNavScaffoldState.currentBottomNavRoute
    val navigationSuiteType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfoV2())

    NavigationSuiteScaffold(
        navigationSuiteType = navigationSuiteType,
        navigationItems = {
            BottomNavTab.entries.forEach { tab ->
                NavigationSuiteItem(
                    navigationSuiteType = navigationSuiteType,
                    selected = currentRoute == tab.route,
                    onClick = { onNavigate(tab.route) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(tab.labelRes), maxLines = 1) }
                )
            }
        }
    ) {
        Scaffold(
            floatingActionButton = {
                AnimatedVisibility(currentRoute == NavigatorSettings && bottomNavScaffoldState.fab != null) {
                    bottomNavScaffoldState.fab?.invoke()
                }
            },
            snackbarHost = { AppSnackbarHost() },
            // Destinations handle safe-area insets in their top bar and content
            contentWindowInsets = WindowInsets(),
            content = content
        )
    }
}
