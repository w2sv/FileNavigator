package com.w2sv.filenavigator.ui.screens.main

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.service.FileListenerService
import com.w2sv.filenavigator.ui.theme.RailwayText
import com.w2sv.filenavigator.utils.goToManageExternalStorageSettings

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(mainScreenViewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current

    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) { granted ->
            if (granted) {
                FileListenerService.start(context)
            }
        }

    Scaffold(snackbarHost = {
        SnackbarHost(mainScreenViewModel.snackbarHostState) { snackbarData ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ManageAllFilesPermissionRequiredSnackbar(snackbarData)
            }
        }
    }) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                    RailwayText(
                        text = stringResource(R.string.navigated_file_types),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.Center) {
                    MediaTypeSelectionGrid(Modifier.fillMaxHeight())
                    this@Column.AnimatedVisibility(
                        visible = mainScreenViewModel.nonAppliedListenerConfiguration.stateChanged.collectAsState().value,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(y = 72.dp)
                    ) {
                        ConfigurationModificationButtonColumn()
                    }
                }

                Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.Center) {
                    ListenerButton(
                        startListener = {
                            when (permissionState.status.isGranted) {
                                true -> FileListenerService.start(context)
                                false -> permissionState.launchPermissionRequest()
                            }
                        },
                        stopListener = { FileListenerService.stop(context) },
                        modifier = Modifier
                            .width(220.dp)
                            .height(80.dp)
                    )
                }
            }
        }
    }

    EventualManageExternalStorageRational()

    BackHandler {
        mainScreenViewModel.onBackPress(context)
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
private fun ManageAllFilesPermissionRequiredSnackbar(snackbarData: SnackbarData) {
    val context = LocalContext.current

    Snackbar(
        action = {
            TextButton(onClick = {
                goToManageExternalStorageSettings(context)
            }) {
                RailwayText(text = stringResource(R.string.grant))
            }
        }
    ) {
        RailwayText(text = snackbarData.visuals.message)
    }
}

@Composable
private fun ListenerButton(
    startListener: () -> Unit,
    stopListener: () -> Unit,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val isListenerRunning by mainScreenViewModel.isListenerRunning.collectAsState()

    ElevatedButton(
        onClick = if (isListenerRunning) stopListener else startListener,
        modifier = modifier
    ) {
        Crossfade(
            targetState = isListenerRunning,
            animationSpec = tween(durationMillis = 1250, delayMillis = 250, easing = EaseOutCubic),
            label = ""
        ) {
            RailwayText(
                text = stringResource(if (it) R.string.stop_listener else R.string.start_listener),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        }
    }
}