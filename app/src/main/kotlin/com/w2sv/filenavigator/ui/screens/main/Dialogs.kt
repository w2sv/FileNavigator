package com.w2sv.filenavigator.ui.screens.main

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.service.FileNavigatorService
import com.w2sv.filenavigator.ui.AppFontText
import com.w2sv.filenavigator.ui.DialogButton

@Composable
internal fun ManageExternalStoragePermissionDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(onClick = {
                onDismissRequest()
                onConfirmation()
            }) {
                AppFontText(text = stringResource(id = R.string.grant))
            }
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_info_24),
                modifier = Modifier.size(32.dp),
                contentDescription = null
            )
        },
        text = { AppFontText(text = stringResource(id = R.string.manage_external_storage_permission_rational)) }
    )
}

@Composable
internal fun PostNotificationsPermissionDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(onClick = onDismissRequest) {
                AppFontText(text = stringResource(id = R.string.got_it))
            }
        },
        icon = {
            Icon(painter = painterResource(id = R.drawable.ic_info_24), contentDescription = null)
        },
        text = {
            AppFontText(
                text = stringResource(R.string.post_notifications_permission_rational)
            )
        }
    )
}

@Composable
internal fun StartNavigatorOnLowBatteryConfirmationDialog(
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    AlertDialog(
        modifier = modifier,
        onDismissRequest = closeDialog,
        confirmButton = {
            DialogButton(
                onClick = {
                    FileNavigatorService.start(context)
                    with(mainScreenViewModel) {
                        disableListenerOnLowBattery.value = false
                        disableListenerOnLowBattery.launchSync()
                    }
                    closeDialog()
                }
            ) {
                AppFontText(text = stringResource(R.string.yes))
            }
        },
        dismissButton = {
            ElevatedButton(onClick = closeDialog) {
                AppFontText(text = stringResource(R.string.no))
            }
        },
        text = {
            AppFontText(text = stringResource(R.string.start_navigator_confirmation_dialog_text))
        }
    )
}