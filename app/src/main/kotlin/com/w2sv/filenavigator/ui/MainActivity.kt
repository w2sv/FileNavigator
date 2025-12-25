package com.w2sv.filenavigator.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var moveDestinationPathConverter: MoveDestinationPathConverter

    private val appVM by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        var triggerStatusBarStyleUpdate by mutableStateOf(false)

        installSplashScreen().setOnExitAnimationListener(
            SwipeRightSplashScreenExitAnimation(
                onAnimationEnd = { triggerStatusBarStyleUpdate = true }
            )
        )

        super.onCreate(savedInstanceState)

        setContent {
            val permissionMissing by appVM.permissions.anyMissing.collectAsStateWithLifecycle()
            @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
            val startScreen = remember {
                when {
                    BuildConfig.DEBUG && BuildConfig.START_SCREEN == "NavigatorSettings" -> Screen.NavigatorSettings
                    permissionMissing -> Screen.RequiredPermissions
                    else -> Screen.Home
                }
            }

            AppUi(
                startScreen = startScreen,
                permissionMissing = { permissionMissing },
                setSystemBarStyles = ::enableEdgeToEdge,
                triggerStatusBarStyleUpdate = triggerStatusBarStyleUpdate,
                moveDestinationPathConverter = moveDestinationPathConverter
            )
        }
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
