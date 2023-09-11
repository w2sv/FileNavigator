package com.w2sv.filenavigator.ui.screens.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppCheckbox
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.DialogButton
import com.w2sv.filenavigator.ui.screens.main.MainScreenViewModel
import kotlinx.coroutines.launch

@Composable
internal fun NavigatorSettingsDialog(
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val onDismissRequest = {
        with(mainScreenViewModel) {
            viewModelScope.launch { unconfirmedExtendedSettings.reset() }
        }
        closeDialog()
    }

    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(id = com.w2sv.navigator.R.drawable.ic_settings_24),
                contentDescription = null
            )
        },
        title = {
            AppFontText(text = stringResource(id = R.string.navigator_settings))
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(
                onClick = {
                    with(mainScreenViewModel) { viewModelScope.launch { unconfirmedExtendedSettings.sync() } }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppFontText(text = stringResource(id = R.string.disable_navigator_on_low_battery))
                    Spacer(modifier = Modifier.weight(1f))
                    AppCheckbox(
                        checked = mainScreenViewModel.unconfirmedDisableListenerOnLowBattery.collectAsState().value,
                        onCheckedChange = {
                            mainScreenViewModel.unconfirmedDisableListenerOnLowBattery.value = it
                        }
                    )
                }
            }
        },
        modifier = modifier
    )
}