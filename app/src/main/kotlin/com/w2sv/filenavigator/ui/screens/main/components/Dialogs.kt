package com.w2sv.filenavigator.ui.screens.main.components

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.DialogButton
import com.w2sv.filenavigator.ui.components.InfoIcon
import com.w2sv.filenavigator.ui.screens.main.MainScreenViewModel
import com.w2sv.navigator.FileNavigator

@Composable
internal fun ManageExternalStoragePermissionDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(
                onClick = {
                    onConfirmation()
                    onDismissRequest()
                }
            ) {
                AppFontText(text = stringResource(id = R.string.grant))
            }
        },
        icon = {
            InfoIcon()
        },
        text = { AppFontText(text = stringResource(id = R.string.manage_external_storage_permission_rational)) },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
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
                AppFontText(text = stringResource(id = R.string.understood))
            }
        },
        icon = {
            InfoIcon()
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
    context: Context = LocalContext.current,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = closeDialog,
        confirmButton = {
            DialogButton(
                onClick = {
                    FileNavigator.start(context)
                    mainScreenViewModel.saveDisableListenerOnLowBattery(false)
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