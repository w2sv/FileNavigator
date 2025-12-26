package com.w2sv.filenavigator.ui

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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.filenavigator.ui.navigation.NavGraph
import com.w2sv.filenavigator.ui.navigation.rememberNavigator
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.theme.useDarkTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var moveDestinationPathConverter: MoveDestinationPathConverter

    private val appVM by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(SwipeRightSplashScreenExitAnimation())

        super.onCreate(savedInstanceState)

        setContent {
            val appThemeSettings by appVM.themeSettings.collectAsStateWithLifecycle()
            val useDarkTheme = useDarkTheme(appThemeSettings.theme)

            // Update system bars on change of useDarkTheme
            LaunchedEffect(useDarkTheme) { enableEdgeToEdge(useDarkTheme = useDarkTheme) }

            val permissionMissing by appVM.permissions.anyMissing.collectAsStateWithLifecycle()
            val navigator = rememberNavigator(startScreen = appVM.startScreen, permissionMissing = { permissionMissing })

            CompositionLocalProvider(
                LocalMoveDestinationPathConverter provides moveDestinationPathConverter,
                LocalNavigator provides navigator
            ) {
                AppTheme(
                    useDarkTheme = useDarkTheme,
                    useAmoledBlackTheme = appThemeSettings.useAmoledBlackTheme,
                    useDynamicColors = appThemeSettings.useDynamicColors
                ) {
                    NavGraph(navigator = navigator)
                }
            }
        }
    }

    private fun enableEdgeToEdge(useDarkTheme: Boolean) {
        val systemBarStyle = if (useDarkTheme) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }

        enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle
        )
    }

    override fun onStart() {
        super.onStart()
        appVM.permissions.updateManageAllFilesPermission()
    }
}

private class SwipeRightSplashScreenExitAnimation(private val onAnimationEnd: () -> Unit = {}) : SplashScreen.OnExitAnimationListener {
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
