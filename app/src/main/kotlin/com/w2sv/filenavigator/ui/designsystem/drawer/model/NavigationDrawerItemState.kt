package com.w2sv.filenavigator.ui.designsystem.drawer.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.Theme
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import com.w2sv.filenavigator.ui.utils.activityViewModel

@Stable
data class NavigationDrawerItemState(
    val theme: () -> Theme,
    val setTheme: (Theme) -> Unit,
    val useAmoledBlackTheme: () -> Boolean,
    val setUseAmoledBlackTheme: (Boolean) -> Unit,
    val useDynamicColors: () -> Boolean,
    val setUseDynamicColors: (Boolean) -> Unit,
)

@Composable
fun rememberNavigationDrawerItemState(appVM: AppViewModel = activityViewModel()): NavigationDrawerItemState {
    val theme by appVM.theme.collectAsStateWithLifecycle()
    val useAmoledBlackTheme by appVM.useAmoledBlackTheme.collectAsStateWithLifecycle()
    val useDynamicColors by appVM.useDynamicColors.collectAsStateWithLifecycle()

    return remember {
        NavigationDrawerItemState(
            theme = { theme },
            setTheme = appVM::saveTheme,
            useAmoledBlackTheme = { useAmoledBlackTheme },
            setUseAmoledBlackTheme = appVM::saveUseAmoledBlackTheme,
            useDynamicColors = { useDynamicColors },
            setUseDynamicColors = appVM::saveUseDynamicColors
        )
    }
}