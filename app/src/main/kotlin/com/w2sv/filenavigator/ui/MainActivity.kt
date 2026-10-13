package com.w2sv.filenavigator.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.core.logging.LoggingComponentActivity
import com.w2sv.core.logging.log
import com.w2sv.core.util.hasManageAllFilesPermission
import com.w2sv.core.util.hasPostNotificationsPermission
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.ui.navigation.main.MainNavRoute
import com.w2sv.filenavigator.ui.shared.debugging.DebugAction
import com.w2sv.filenavigator.ui.shared.permissions.AppPermissionsState
import com.w2sv.kotlinutils.makeIf
import com.w2sv.kotlinutils.threadUnsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : LoggingComponentActivity() {

    @Inject
    lateinit var moveDestinationLabelProvider: MoveDestinationLabelProvider

    private val viewModel by viewModels<MainActivityViewModel>()
    private val permissionsState by threadUnsafeLazy {
        AppPermissionsState(
            hasPostNotificationsPermission = { hasPostNotificationsPermission() },
            hasManageAllFilesPermission = { hasManageAllFilesPermission },
            postNotificationsPermissionHistory = viewModel.postNotificationsPermissionHistory
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(SwipeRightExitAnimation())
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)

        val debugAction = makeIf(BuildConfig.DEBUG) { DebugAction.fromIntentAction(intent.action.log { "intent action=$it" }) }
        val startRoute = if (permissionsState.allGranted.value) MainNavRoute.BottomNavHost() else MainNavRoute.Permissions

        setContent {
            val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()
            FileNavigatorUi(
                themeSettings = themeSettings,
                startRoute = startRoute,
                permissionsState = permissionsState,
                moveDestinationLabelProvider = moveDestinationLabelProvider,
                debugAction = debugAction
            )
        }
    }

    override fun onResume() {
        super.onResume()
        permissionsState.refresh()
    }
}

private class SwipeRightExitAnimation(private val onAnimationEnd: () -> Unit = {}) : SplashScreen.OnExitAnimationListener {
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
