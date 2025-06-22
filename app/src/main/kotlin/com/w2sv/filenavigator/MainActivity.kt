package com.w2sv.filenavigator

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.model.Theme
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.filenavigator.ui.LocalMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.LocalPostNotificationsPermissionState
import com.w2sv.filenavigator.ui.LocalUseDarkTheme
import com.w2sv.filenavigator.ui.navigation.NavGraph
import com.w2sv.filenavigator.ui.state.rememberPostNotificationsPermissionState
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appVM by viewModels<AppViewModel>()

    @Inject
    lateinit var moveDestinationPathConverter: MoveDestinationPathConverter

    override fun onCreate(savedInstanceState: Bundle?) {
        var triggerStatusBarStyleUpdate by mutableStateOf(false)

        installSplashScreen().setOnExitAnimationListener(
            SwipeRightSplashScreenExitAnimation(
                onAnimationEnd = { triggerStatusBarStyleUpdate = true }
            )
        )

        super.onCreate(savedInstanceState)

        setContent {
            val theme by appVM.theme.collectAsStateWithLifecycle()
            val useDarkTheme = useDarkTheme(theme = theme)
            val useAmoledBlackTheme by appVM.useAmoledBlackTheme.collectAsStateWithLifecycle()
            val useDynamicColors by appVM.useDynamicColors.collectAsStateWithLifecycle()
            val anyPermissionMissing by appVM.permissions.anyMissing.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalUseDarkTheme provides useDarkTheme) {
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

                        enableEdgeToEdge(
                            systemBarStyle,
                            systemBarStyle
                        )
                    }

                    CompositionLocalProvider(
                        LocalMoveDestinationPathConverter provides moveDestinationPathConverter,
                        LocalPostNotificationsPermissionState provides rememberPostNotificationsPermissionState(
                            onPermissionResult = { appVM.permissions.savePostNotificationsRequested() },
                            onStatusChanged = appVM.permissions::setPostNotificationsGranted
                        )
                    ) {
                        NavGraph(anyPermissionMissing = anyPermissionMissing)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appVM.permissions.updateManageAllFilesPermission()
    }
}

@ReadOnlyComposable
@Composable
private fun useDarkTheme(theme: Theme): Boolean =
    when (theme) {
        Theme.Light -> false
        Theme.Dark -> true
        Theme.Default -> isSystemInDarkTheme()
    }

private class SwipeRightSplashScreenExitAnimation(private val onAnimationEnd: () -> Unit = {}) :
    SplashScreen.OnExitAnimationListener {
    override fun onSplashScreenExit(splashScreenViewProvider: SplashScreenViewProvider) {
        ObjectAnimator.ofFloat(
            splashScreenViewProvider.view,
            View.TRANSLATION_X,
            0f,
            splashScreenViewProvider.view.width.toFloat()
        )
            .apply {
                interpolator = AnticipateInterpolator()
                duration = 400L
                doOnEnd {
                    splashScreenViewProvider.remove()
                    onAnimationEnd()
                }
            }
            .start()
    }
}
