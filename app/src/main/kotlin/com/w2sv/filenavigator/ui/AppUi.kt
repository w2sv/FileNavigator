package com.w2sv.filenavigator.ui

import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.Theme
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.filenavigator.ui.navigation.NavGraph
import com.w2sv.filenavigator.ui.state.rememberPostNotificationsPermissionState
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.util.activityViewModel

@Composable
fun AppUi(
    setSystemBarStyles: (SystemBarStyle, SystemBarStyle) -> Unit,
    triggerStatusBarStyleUpdate: Boolean,
    moveDestinationPathConverter: MoveDestinationPathConverter,
    appVM: AppViewModel = activityViewModel()
) {
    val theme by appVM.theme.collectAsStateWithLifecycle()
    val useDarkTheme = rememberUseDarkTheme(theme = theme)
    val useAmoledBlackTheme by appVM.useAmoledBlackTheme.collectAsStateWithLifecycle()
    val useDynamicColors by appVM.useDynamicColors.collectAsStateWithLifecycle()
    val anyPermissionMissing by appVM.permissions.anyMissing.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalUseDarkTheme provides useDarkTheme,
        LocalMoveDestinationPathConverter provides moveDestinationPathConverter,
        LocalPostNotificationsPermissionState provides rememberPostNotificationsPermissionState(
            onPermissionResult = { appVM.permissions.savePostNotificationsRequested() },
            onStatusChanged = appVM.permissions::setPostNotificationsGranted
        )
    ) {
        AppTheme(
            useDarkTheme = useDarkTheme,
            useAmoledBlackTheme = useAmoledBlackTheme,
            useDynamicColors = useDynamicColors
        ) {
            // Reset system bar styles on theme change
            LaunchedEffect(useDarkTheme, triggerStatusBarStyleUpdate) {
                val systemBarStyle = if (useDarkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }

                setSystemBarStyles(
                    systemBarStyle,
                    systemBarStyle
                )
            }

            NavGraph(anyPermissionMissing = anyPermissionMissing)
        }
    }
}

@Composable
private fun rememberUseDarkTheme(theme: Theme): Boolean {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    return remember(theme, isSystemInDarkTheme) {
        when (theme) {
            Theme.Light -> false
            Theme.Dark -> true
            Theme.Default -> isSystemInDarkTheme
        }
    }
}
