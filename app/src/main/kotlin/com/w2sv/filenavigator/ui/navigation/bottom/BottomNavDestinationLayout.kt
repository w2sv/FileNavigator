package com.w2sv.filenavigator.ui.navigation.bottom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.window.core.layout.WindowSizeClass
import com.w2sv.composed.ui.thenIfNotNull

@Composable
internal fun BottomNavDestinationLayout(route: BottomNavRoute, modifier: Modifier = Companion, content: @Composable () -> Unit) {
    val useCompactTopAppBar = useCompactTopBar()
    val scrollBehavior = when {
        route == Home -> null
        useCompactTopAppBar -> TopAppBarDefaults.enterAlwaysScrollBehavior()
        else -> TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    }

    val title: @Composable () -> Unit = {
        Text(
            text = stringResource(route.titleRes),
            maxLines = 1
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .thenIfNotNull(scrollBehavior) { nestedScroll(it.nestedScrollConnection) }
    ) {
        if (useCompactTopAppBar) {
            TopAppBar(
                title = title,
                scrollBehavior = scrollBehavior
            )
        } else {
            MediumTopAppBar(
                title = title,
                scrollBehavior = scrollBehavior
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                // Make background opaque for predictive pop transition
                .background(MaterialTheme.colorScheme.background)
                // Top insets are already provided by the top bar
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
        ) {
            content()
        }
    }
}

@Composable
private fun useCompactTopBar(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    return !windowSizeClass.isHeightAtLeastBreakpoint(
        WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
    )
}
