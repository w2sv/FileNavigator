package com.w2sv.filenavigator

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.lifecycleScope
import com.w2sv.common.utils.collectFromFlow
import com.w2sv.data.model.Theme
import com.w2sv.filenavigator.ui.screens.AppViewModel
import com.w2sv.filenavigator.ui.screens.main.MainScreen
import com.w2sv.filenavigator.ui.screens.main.MainScreenViewModel
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.PowerSaveModeChangedReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var fileNavigatorStatusChanged: FileNavigator.StatusChanged

    private val appVM by viewModels<AppViewModel>()
    private val mainScreenVM by viewModels<MainScreenViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(SwipeRightSplashScreenExitAnimation())

        super.onCreate(savedInstanceState)

        lifecycleScope.collectFromFlows()

        setContent {
            AppTheme(
                useDarkTheme = when (appVM.theme.collectAsState().value) {
                    Theme.Dark -> true
                    Theme.Light -> false
                    Theme.DeviceDefault -> isSystemInDarkTheme()
                }
            ) {
                MainScreen()
            }
        }
    }

    private fun CoroutineScope.collectFromFlows() {
        collectFromFlow(fileNavigatorStatusChanged.isRunning) {
            mainScreenVM.navigatorUIState.isRunning.value = it
        }

        collectFromFlow(appVM.exitApplication) {
            finishAffinity()
        }

        collectFromFlow(mainScreenVM.navigatorUIState.disableOnLowBattery) {
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

        mainScreenVM.storageAccessState.updateStatus(this)
    }
}

private class SwipeRightSplashScreenExitAnimation : SplashScreen.OnExitAnimationListener {
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
                }
            }
            .start()
    }
}