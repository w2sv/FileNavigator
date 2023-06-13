package com.w2sv.filenavigator.ui.screens.main

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.service.FileNavigatorService
import com.w2sv.filenavigator.ui.AppSnackbar
import com.w2sv.filenavigator.ui.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.filenavigator.ui.theme.RailwayText
import com.w2sv.filenavigator.ui.theme.md_negative
import com.w2sv.filenavigator.ui.theme.md_positive
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(mainScreenViewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) { granted ->
            if (granted) {
                FileNavigatorService.start(context)
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
                Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.CenterStart) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RailwayText(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        SettingsDialogButton(onClick = { showSettingsDialog = true })
                    }
                }

                Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.Center) {
                    FileTypeAccordionColumn(Modifier.fillMaxHeight())

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
                                    mainScreenViewModel.snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                        snackbarVisuals
                                    )
                                }
                            }
                        )
                    }
                }

                Box(modifier = Modifier.weight(0.25f), contentAlignment = Alignment.Center) {
                    StartNavigatorButton(
                        startListener = {
                            when (permissionState.status.isGranted) {
                                true -> FileNavigatorService.start(context)
                                false -> permissionState.launchPermissionRequest()
                            }
                        },
                        stopListener = { FileNavigatorService.stop(context) },
                        modifier = Modifier
                            .width(220.dp)
                            .height(70.dp)
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

@Composable
private fun StartNavigatorButton(
    startListener: () -> Unit,
    stopListener: () -> Unit,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val isNavigatorRunning by mainScreenViewModel.isNavigatorRunning.collectAsState()

    Crossfade(
        targetState = isNavigatorRunning,
        animationSpec = tween(durationMillis = 1250, delayMillis = 250, easing = EaseOutCubic),
        label = ""
    ) {
        val properties = when (it) {
            true -> NavigatorButtonProperties(
                md_negative,
                R.drawable.ic_stop_24,
                R.string.stop_navigator,
                stopListener
            )

            false -> NavigatorButtonProperties(
                md_positive,
                R.drawable.ic_start_24,
                R.string.start_navigator,
                startListener
            )
        }

        ElevatedButton(
            onClick = properties.onClick,
            modifier = modifier,
//            colors = ButtonDefaults.elevatedButtonColors(
//                containerColor = properties.color.copy(
//                    alpha = 0.6f
//                )
//            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = properties.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = properties.color
                )
                RailwayText(
                    text = stringResource(id = properties.labelRes),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

private data class NavigatorButtonProperties(
    val color: Color,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit
)