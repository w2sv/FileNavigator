package com.w2sv.filenavigator.ui.screens.main

import android.Manifest
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) {
            if (it) {
                FileListenerService.start(context)
            }
        }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(all = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            RailwayText(
                text = stringResource(R.string.select_media_types),
                style = MaterialTheme.typography.headlineMedium
            )
            MediaTypeSelectionGrid(modifier = Modifier.heightIn(360.dp))
//            Divider(modifier = Modifier.padding(16.dp))
            ListenerButton(
                startListener = {
                    when (permissionState.status.isGranted) {
                        true -> FileListenerService.start(context)
                        false -> permissionState.launchPermissionRequest()
                    }
                },
                stopListener = { FileListenerService.stop(context) }
            )
        }
    }
}

@Composable
fun ListenerButton(
    startListener: () -> Unit,
    stopListener: () -> Unit,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val isListenerRunning by mainScreenViewModel.isListenerRunning.collectAsState()

    ElevatedButton(
        onClick = if (isListenerRunning) stopListener else startListener,
        modifier = modifier
            .width(220.dp)
            .height(80.dp)
    ) {
        Crossfade(
            targetState = isListenerRunning,
            animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
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