package com.w2sv.filenavigator.ui.screens.main

import android.Manifest
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.w2sv.filenavigator.powerSaveModeActivated
import com.w2sv.filenavigator.service.FileNavigatorService
import com.w2sv.filenavigator.ui.theme.RailwayText
import com.w2sv.filenavigator.ui.theme.md_negative
import com.w2sv.filenavigator.ui.theme.md_positive

private data class NavigatorButtonProperties(
    val color: Color,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun StartNavigatorButton(
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    val isNavigatorRunning by mainScreenViewModel.isNavigatorRunning.collectAsState()
    var showConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                StartNavigatorConfirmationDialog(closeDialog = { value = false })
            }
        }

    val startNavigatorOrShowConfirmationDialog = {
        if (mainScreenViewModel.disableListenerOnLowBattery.value && context.powerSaveModeActivated == true) {
            showConfirmationDialog = true
        } else {
            FileNavigatorService.start(context)
        }
    }
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) { granted ->
            if (granted) {
                startNavigatorOrShowConfirmationDialog()
            }
        }

    Crossfade(
        targetState = isNavigatorRunning,
        animationSpec = tween(durationMillis = 1250, delayMillis = 250, easing = EaseOutCubic),
        label = ""
    ) {
        val properties = when (it) {
            true -> NavigatorButtonProperties(
                md_negative,
                R.drawable.ic_stop_24,
                R.string.stop_navigator
            ) { FileNavigatorService.stop(context) }

            false -> NavigatorButtonProperties(
                md_positive,
                R.drawable.ic_start_24,
                R.string.start_navigator
            ) {
                when (permissionState.status.isGranted) {
                    true -> startNavigatorOrShowConfirmationDialog()
                    false -> permissionState.launchPermissionRequest()
                }
            }
        }

        ElevatedButton(
            onClick = properties.onClick,
            modifier = modifier
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

@Composable
private fun StartNavigatorConfirmationDialog(
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    AlertDialog(
        modifier = modifier,
        onDismissRequest = closeDialog,
        confirmButton = {
            ElevatedButton(onClick = {
                FileNavigatorService.start(context)
                with(mainScreenViewModel) {
                    disableListenerOnLowBattery.value = false
                    disableListenerOnLowBattery.launchSync()
                }
                closeDialog()
            }) {
                RailwayText(text = stringResource(R.string.yes))
            }
        },
        dismissButton = {
            ElevatedButton(onClick = closeDialog) {
                RailwayText(text = stringResource(R.string.no))
            }
        },
        text = {
            RailwayText(text = stringResource(R.string.start_navigator_confirmation_dialog_text))
        }
    )
}
