package com.w2sv.filenavigator

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.lifecycle.SelfManagingLocalBroadcastReceiver
import com.w2sv.data.model.Theme
import com.w2sv.navigator.PowerSaveModeChangedReceiver
import com.w2sv.filenavigator.ui.screens.main.MainScreen
import com.w2sv.filenavigator.ui.screens.main.MainScreenViewModel
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.navigator.service.FileNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import slimber.log.i

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainScreenViewModel>()

    private class FileListenerStatusChangedReceiver(
        context: Context,
        callback: (Context?, Intent?) -> Unit
    ) : SelfManagingLocalBroadcastReceiver.Impl(
        context,
        IntentFilter()
            .apply {
                addAction(FileNavigator.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED)
                addAction(FileNavigator.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED)
            },
        callback
    )

    private val fileListenerStatusChangedReceiver by lazy {
        FileListenerStatusChangedReceiver(this) { _, intent ->
            intent ?: return@FileListenerStatusChangedReceiver

            viewModel.isNavigatorRunning.value = when (intent.action) {
                FileNavigator.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED -> true
                FileNavigator.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED -> false
                else -> throw Error()
            }
        }
    }

    private val defaultDestinationSelectionLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri ->
            i { "DocumentTree Uri: $treeUri" }

            if (treeUri != null) {
                DocumentFile.fromTreeUri(this, treeUri)?.let { documentFile ->
                    viewModel.unconfirmedDefaultMoveDestination!!.value = documentFile.uri
                }
            }

            viewModel.launchDefaultMoveDestinationPickerFor.value = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener(SwipeRightSplashScreenExitAnimation())

        super.onCreate(savedInstanceState)

        lifecycle.addObserver(fileListenerStatusChangedReceiver)

        lifecycleScope.collectFlows()

        setContent {
            AppTheme(
                useDarkTheme = when (viewModel.inAppTheme.collectAsState(initial = Theme.DeviceDefault).value) {
                    Theme.Dark -> true
                    Theme.Light -> false
                    Theme.DeviceDefault -> isSystemInDarkTheme()
                }
            ) {
                MainScreen()
            }
        }
    }

    private fun LifecycleCoroutineScope.collectFlows() {
        launch {
            viewModel.exitApplication.collect {
                finishAffinity()
            }
        }

        launch {
            viewModel.launchDefaultMoveDestinationPickerFor.collect {
                if (it != null) {
                    defaultDestinationSelectionLauncher.launch(null)
                }  // TODO
            }
        }

        launch {
            viewModel.disableListenerOnLowBattery.collect {
                i { "Collected disableListenerOnLowBattery=$it" }

                val intent = PowerSaveModeChangedReceiver.HostService.getIntent(this@MainActivity)
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
    }

    override fun onStart() {
        super.onStart()

        viewModel.updateStorageAccessStatus(this)
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