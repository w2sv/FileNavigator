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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.RequiredPermissionsScreenDestination
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import com.w2sv.composed.OnChange
import com.w2sv.domain.model.Theme
import com.w2sv.filenavigator.ui.LocalMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.LocalNavHostController
import com.w2sv.filenavigator.ui.LocalUseDarkTheme
import com.w2sv.filenavigator.ui.state.rememberObservedPostNotificationsPermissionState
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.viewmodel.AppViewModel
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
            CompositionLocalProvider(
                LocalUseDarkTheme provides useDarkTheme(theme = appVM.theme.collectAsStateWithLifecycle().value)
            ) {
                val useDarkTheme = LocalUseDarkTheme.current
                AppTheme(
                    useDarkTheme = useDarkTheme,
                    useAmoledBlackTheme = appVM.useAmoledBlackTheme.collectAsStateWithLifecycle().value,
                    useDynamicColors = appVM.useDynamicColors.collectAsStateWithLifecycle().value
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
                    val navController = rememberNavController()

                    val postNotificationsPermissionState =
                        rememberObservedPostNotificationsPermissionState(
                            onPermissionResult = { appVM.savePostNotificationsPermissionRequestedIfRequired() },
                            onStatusChanged = appVM::setPostNotificationsPermissionGranted
                        )

                    val allPermissionsGranted by appVM.allPermissionsGranted.collectAsStateWithLifecycle()
                    OnChange(allPermissionsGranted) {
                        if (!allPermissionsGranted) {
                            FileNavigator.stop(this)

                            if (!navController.isRouteOnBackStack(
                                    RequiredPermissionsScreenDestination
                                )
                            ) {
                                navController.navigate(
                                    direction = RequiredPermissionsScreenDestination,
                                    navOptionsBuilder = {
                                        launchSingleTop = true
                                        popUpTo(RequiredPermissionsScreenDestination)
                                    }
                                )
                            }
                        }
                    }

                    CompositionLocalProvider(
                        LocalNavHostController provides navController,
                        LocalMoveDestinationPathConverter provides appVM.moveDestinationPathConverter
                    ) {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            DestinationsNavHost(
                                navGraph = NavGraphs.root,
                                navController = navController,
                                dependenciesContainerBuilder = {
                                    dependency(postNotificationsPermissionState)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        appVM.updateManageAllFilesPermissionGranted()
    }
}

@ReadOnlyComposable
@Composable
private fun useDarkTheme(theme: Theme): Boolean {
    return when (theme) {
        Theme.Light -> false
        Theme.Dark -> true
        Theme.Default -> isSystemInDarkTheme()
    }
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
