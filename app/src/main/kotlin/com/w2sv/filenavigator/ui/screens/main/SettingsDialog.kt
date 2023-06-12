package com.w2sv.filenavigator.ui.screens.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.theme.RailwayText
import com.w2sv.filenavigator.utils.toggle

@Composable
internal fun SettingsDialogButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_settings_24),
            contentDescription = stringResource(
                id = R.string.open_extended_settings_dialog
            ),
            tint = MaterialTheme.colorScheme.secondary
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
            disableListenerOnLowBattery.launchReset()
            closeDialog()
        }
    }

    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings_24),
                contentDescription = null
            )
        },
        title = {
            RailwayText(
                text = stringResource(id = R.string.extended_settings)
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ElevatedButton(
                onClick = {
                    with(mainScreenViewModel) { disableListenerOnLowBattery.launchSync() }
                    closeDialog()
                },
                enabled = mainScreenViewModel.disableListenerOnLowBattery.statesDissimilar.collectAsState().value
            ) {
                RailwayText(text = stringResource(id = R.string.apply))
            }
        },
        dismissButton = {
            ElevatedButton(onClick = onDismissRequest) {
                RailwayText(text = stringResource(id = R.string.cancel))
            }
        },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RailwayText(text = stringResource(id = R.string.disable_listener_on_low_battery))
                Spacer(modifier = Modifier.weight(1f))
                Checkbox(
                    checked = mainScreenViewModel.disableListenerOnLowBattery.collectAsState().value,
                    onCheckedChange = { mainScreenViewModel.disableListenerOnLowBattery.toggle() }
                )
            }
        },
        modifier = modifier
    )
}