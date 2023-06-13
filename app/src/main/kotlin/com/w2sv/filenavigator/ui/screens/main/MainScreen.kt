package com.w2sv.filenavigator.ui.screens.main

import android.Manifest
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.w2sv.filenavigator.ui.AppSnackbar
import com.w2sv.filenavigator.ui.theme.RailwayText
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(mainScreenViewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) { granted ->
            if (granted) {
                FileListenerService.start(context)
            }
        }

    var showSettingsDialog by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                SettingsDialog(closeDialog = { value = false })
            }
        }

    Scaffold(snackbarHost = {
        SnackbarHost(mainScreenViewModel.snackbarHostState) { snackbarData ->
            AppSnackbar(snackbarData = snackbarData)
        }
    }) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.Center) {
                    RailwayText(
                        text = stringResource(R.string.navigated_file_types),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) {
                    MediaTypeSelectionGrid(Modifier.fillMaxHeight())
                    this@Column.AnimatedVisibility(
                        visible = mainScreenViewModel.unconfirmedListenerConfiguration.statesDissimilar.collectAsState().value,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(y = 72.dp)
                    ) {
                        ListenerModificationButtonColumn(
                            showSnackbar = { snackbarVisuals ->
                                scope.launch {
                                    with(mainScreenViewModel.snackbarHostState) {
                                        currentSnackbarData?.dismiss()
                                        showSnackbar(snackbarVisuals)
                                    }
                                }
                            }
                        )
                    }
                }

                Box(modifier = Modifier.weight(0.25f), contentAlignment = Alignment.Center) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LaunchListenerButton(
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
                        SettingsDialogButton(onClick = { showSettingsDialog = true })
                    }
                }
            }
        }
    }

    EventualManageExternalStorageRational()

    BackHandler {
        mainScreenViewModel.onBackPress(context)
    }
}

@Composable
private fun LaunchListenerButton(
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