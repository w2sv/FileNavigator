package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.lifecycle.SelfManagingLocalBroadcastReceiver
import com.w2sv.filenavigator.service.FileListenerService
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainScreenViewModel>()

    private class FileListenerServiceBroadcastReceiver(
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

    private val fileListenerServiceBroadcastReceiver by lazy {
        FileListenerServiceBroadcastReceiver(this) { _, intent ->
            intent ?: return@FileListenerServiceBroadcastReceiver

            viewModel.isListenerRunning.value = when (intent.action) {
                FileListenerService.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STARTED -> true
                FileListenerService.ACTION_NOTIFY_FILE_LISTENER_SERVICE_STOPPED -> false
                else -> throw Error()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        lifecycle.addObserver(fileListenerServiceBroadcastReceiver)

        lifecycleScope.launch {
            viewModel.exitApplication.collect {
                finishAffinity()
            }
        }

        setContent {
            FileNavigatorTheme {
                MainScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.updateManageExternalStoragePermissionGranted()
    }
}