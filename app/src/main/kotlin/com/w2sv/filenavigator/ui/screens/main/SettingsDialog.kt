package com.w2sv.filenavigator.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.AppCheckbox
import com.w2sv.filenavigator.ui.DialogButton
import com.w2sv.filenavigator.ui.AppFontText
import com.w2sv.filenavigator.ui.ThemeSelectionRow

@Composable
internal fun SettingsDialogButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_settings_24),
            contentDescription = stringResource(
                id = R.string.open_extended_settings_dialog
            ),
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
internal fun SettingsDialog(
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val onDismissRequest = {
        with(mainScreenViewModel) {
            unconfirmedExtendedSettings.launchReset()
        }
        closeDialog()
    }

    val inAppTheme by mainScreenViewModel.inAppTheme.collectAsState()

    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings_24),
                contentDescription = null
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(
                onClick = {
                    with(mainScreenViewModel) { unconfirmedExtendedSettings.launchSync() }
                    closeDialog()
                },
                enabled = mainScreenViewModel.unconfirmedExtendedSettings.statesDissimilar.collectAsState().value
            ) {
                AppFontText(text = stringResource(id = R.string.apply))
            }
        },
        dismissButton = {
            DialogButton(onClick = onDismissRequest) {
                AppFontText(text = stringResource(id = R.string.cancel))
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppFontText(
                    text = stringResource(R.string.theme),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))

                ThemeSelectionRow(
                    selected = { inAppTheme },
                    onSelected = { mainScreenViewModel.inAppTheme.value = it })

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppFontText(text = stringResource(id = R.string.disable_listener_on_low_battery))
                    Spacer(modifier = Modifier.weight(1f))
                    AppCheckbox(
                        checked = mainScreenViewModel.disableListenerOnLowBattery.collectAsState().value,
                        onCheckedChange = {
                            mainScreenViewModel.disableListenerOnLowBattery.value = it
                        }
                    )
                }
            }
        },
        modifier = modifier
    )
}