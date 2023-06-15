package com.w2sv.filenavigator

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.lifecycle.SelfManagingLocalBroadcastReceiver
import com.w2sv.filenavigator.service.FileNavigatorService
import com.w2sv.filenavigator.ui.Theme
import com.w2sv.filenavigator.ui.screens.main.MainScreen
import com.w2sv.filenavigator.ui.screens.main.MainScreenViewModel
import com.w2sv.filenavigator.ui.theme.AppTheme
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
                addAction(FileNavigatorService.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED)
                addAction(FileNavigatorService.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED)
            },
        callback
    )

    private val fileListenerStatusChangedReceiver by lazy {
        FileListenerStatusChangedReceiver(this) { _, intent ->
            intent ?: return@FileListenerStatusChangedReceiver

            viewModel.isNavigatorRunning.value = when (intent.action) {
                FileNavigatorService.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED -> true
                FileNavigatorService.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED -> false
                else -> throw Error()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        lifecycle.addObserver(fileListenerStatusChangedReceiver)

        lifecycleScope.collectFlows()

        setContent {
            AppTheme(
                useDarkTheme = when (viewModel.repository.inAppTheme.collectAsState(initial = Theme.DeviceDefault).value) {
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
            viewModel.repository.disableListenerOnLowBattery.collect {
                i { "Collected disableListenerOnLowBattery=$it" }

                when (it) {
                    true -> {
                        startService(PowerSaveModeChangedReceiver.HostService.getIntent(this@MainActivity))
                    }

                    false -> {
                        stopService(PowerSaveModeChangedReceiver.HostService.getIntent(this@MainActivity))
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.updateManageExternalStoragePermissionGranted()
    }
}