package com.w2sv.filenavigator.ui.screens.main.components

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.DialogButton
import com.w2sv.filenavigator.ui.components.InfoIcon

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