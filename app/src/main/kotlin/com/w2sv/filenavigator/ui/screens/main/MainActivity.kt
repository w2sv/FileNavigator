package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.lifecycle.SelfManagingLocalBroadcastReceiver
import com.w2sv.filenavigator.PowerSaveModeChangedReceiver
import com.w2sv.filenavigator.service.FileListenerService
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme
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
                addAction(FileListenerService.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED)
                addAction(FileListenerService.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED)
            },
        callback
    )

    private val fileListenerStatusChangedReceiver by lazy {
        FileListenerStatusChangedReceiver(this) { _, intent ->
            intent ?: return@FileListenerStatusChangedReceiver

            viewModel.isListenerRunning.value = when (intent.action) {
                FileListenerService.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED -> true
                FileListenerService.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED -> false
                else -> throw Error()
            }
        }
    }

    private val powerSaveModeChangedReceiver by lazy {
        PowerSaveModeChangedReceiver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        lifecycle.addObserver(fileListenerStatusChangedReceiver)

        lifecycleScope.collectFlows()

        setContent {
            FileNavigatorTheme {
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

                try {
                    when (it) {
                        true -> {
                            registerReceiver(
                                powerSaveModeChangedReceiver,
                                IntentFilter()
                                    .apply {
                                        addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
                                    }
                            )
                            i { "Registered ${powerSaveModeChangedReceiver::class.java.simpleName}" }
                        }

                        false -> {
                            Intent.ACTION_BATTERY_CHANGED
                            unregisterReceiver(powerSaveModeChangedReceiver)
                            i { "Unregistered ${powerSaveModeChangedReceiver::class.java.simpleName}" }
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    i(e)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.updateManageExternalStoragePermissionGranted()
    }
}