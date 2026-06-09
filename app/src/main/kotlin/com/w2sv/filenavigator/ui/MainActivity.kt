package com.w2sv.filenavigator.ui

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
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
import com.w2sv.common.logging.LoggingComponentActivity
import com.w2sv.common.logging.log
import com.w2sv.composed.core.CollectFromFlow
import com.w2sv.designsystem.modelext.useDarkTheme
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.ui.navigation.NavGraph
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.navigation.rememberNavigator
import com.w2sv.filenavigator.ui.shared.LocalMoveDestinationLabelProvider
import com.w2sv.filenavigator.ui.shared.debugging.DevAction
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : LoggingComponentActivity() {

    @Inject
    lateinit var moveDestinationLabelProvider: MoveDestinationLabelProvider

    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(SwipeRightSplashScreenExitAnimation())

        super.onCreate(savedInstanceState)

        setContent {
            val appThemeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()
            val useDarkTheme = useDarkTheme(appThemeSettings.theme)

            // Update system bars on change of useDarkTheme
            LaunchedEffect(useDarkTheme) { enableEdgeToEdge(useDarkTheme = useDarkTheme) }

            val navigator = rememberNavigator(startScreen = viewModel.startScreen)

            // Navigate to permissions screen if not all permissions granted and we're not already there
            CollectFromFlow(viewModel.permissionsState.allGranted) { allPermissionsGranted ->
                if (!allPermissionsGranted && !navigator.currentScreen.isPermissions) {
                    navigator.toPermissions()
                }
            }

            CompositionLocalProvider(LocalMoveDestinationLabelProvider provides moveDestinationLabelProvider) {
                AppTheme(
                    useDarkTheme = useDarkTheme,
                    useAmoledBlackTheme = appThemeSettings.useAmoledBlackTheme,
                    useDynamicColors = appThemeSettings.useDynamicColors
                ) {
                    NavGraph(
                        navigator = navigator,
                        permissionsState = viewModel.permissionsState
                    )
                }
            }

            if (BuildConfig.DEBUG) {
                LaunchedEffect(Unit) { performOptionalDevAction(navigator) }
            }
        }
    }

    private fun performOptionalDevAction(navigator: Navigator) {
        when (intent.action.log { "action=$it" }) {
            DevAction.LAUNCH_NAVIGATOR_SETTINGS_SCREEN -> navigator.toNavigatorSettings()
            DevAction.START_NAVIGATOR -> FileNavigator.start(this)
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

    override fun onResume() {
        super.onResume()
        viewModel.permissionsState.refresh()
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
