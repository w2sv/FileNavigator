package com.w2sv.filenavigator

import android.Manifest
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MissingPermissionsScreenDestination
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.utils.isRouteOnBackStack
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.common.utils.postNotificationsPermissionRequired
import com.w2sv.composed.OnChange
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import com.w2sv.filenavigator.ui.sharedviewmodels.NavigatorViewModel
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.theme.useDarkTheme
import com.w2sv.filenavigator.ui.utils.LocalNavHostController
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.PowerSaveModeChangedReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import slimber.log.i

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appVM by viewModels<AppViewModel>()
    private val navigatorVM by viewModels<NavigatorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(
            SwipeRightSplashScreenExitAnimation(
                onAnimationEnd = { enableEdgeToEdge() }
            )
        )

        super.onCreate(savedInstanceState)

        lifecycleScope.collectFromFlows()

        setContent {
            val useDarkTheme = useDarkTheme(theme = appVM.theme.collectAsStateWithLifecycle().value)

            AppTheme(
                useDynamicColors = appVM.useDynamicColors.collectAsStateWithLifecycle().value,
                useDarkTheme = useDarkTheme,
                useAmoledBlackTheme = appVM.useAmoledBlackTheme.collectAsStateWithLifecycle().value
            ) {
                // Reset system bar styles on theme change
                OnChange(useDarkTheme) {
                    val systemBarStyle = if (it) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }

                    enableEdgeToEdge(
                        systemBarStyle,
                        systemBarStyle,
                    )
                }

                val postNotificationsPermissionState =
                    rememberObservedPostNotificationsPermissionState(
                        onPermissionResult = { appVM.savePostNotificationsPermissionRequestedIfRequired() },
                        onStatusChanged = appVM::setPostNotificationsPermissionGranted
                    )

                val navController = rememberNavController()

                OnChange(appVM.allPermissionsGranted.collectAsStateWithLifecycle().value) { allPermissionsGranted ->
                    if (!allPermissionsGranted && !navController.isRouteOnBackStack(
                            MissingPermissionsScreenDestination
                        )
                    ) {
                        navController.navigate(
                            direction = MissingPermissionsScreenDestination,
                            navOptionsBuilder = {
                                launchSingleTop = true
                                popUpTo(MissingPermissionsScreenDestination)
                            }
                        )
                    } else if (allPermissionsGranted && navController.isRouteOnBackStack(
                            MissingPermissionsScreenDestination
                        )
                    ) {
                        navController.navigate(
                            direction = HomeScreenDestination,
                            navOptionsBuilder = {
                                launchSingleTop = true
                                popUpTo(HomeScreenDestination)
                            }
                        )
                    }
                }

                CompositionLocalProvider(
                    LocalNavHostController provides navController
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        DestinationsNavHost(
                            navGraph = NavGraphs.root,
                            navController = navController,
                            dependenciesContainerBuilder = {
                                dependency(postNotificationsPermissionState)
                            },
                        )
                    }
                }
            }
        }
    }

    private fun CoroutineScope.collectFromFlows() {
        collectFromFlow(navigatorVM.configuration.disableOnLowBattery.appliedState) {
            val intent = PowerSaveModeChangedReceiver.HostService.getIntent(this@MainActivity)

            i { "Collected disableListenerOnLowBattery=$it" }

            when (it) {
                true -> {
                    startService(intent)
                }

                false -> {
                    stopService(intent)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        appVM.updateManageAllFilesPermissionGranted().let { isGranted ->
            if (!isGranted && navigatorVM.isRunning.value) {
                FileNavigator.stop(this)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun rememberObservedPostNotificationsPermissionState(
    onPermissionResult: (Boolean) -> Unit,
    onStatusChanged: (Boolean) -> Unit
): PostNotificationsPermissionState =
    PostNotificationsPermissionState(
        state = if (postNotificationsPermissionRequired()) {
            rememberPermissionState(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                onPermissionResult = onPermissionResult
            )
                .also {
                    OnChange(value = it.status) { status ->
                        onStatusChanged(status.isGranted)
                    }
                }
        } else {
            null
        }
    )

@Immutable
data class PostNotificationsPermissionState @OptIn(ExperimentalPermissionsApi::class) constructor(
    val state: PermissionState?
)

private class SwipeRightSplashScreenExitAnimation(private val onAnimationEnd: () -> Unit) :
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