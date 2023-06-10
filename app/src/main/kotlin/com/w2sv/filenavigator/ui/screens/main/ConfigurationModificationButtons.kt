package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.service.FileListenerService

@Composable
internal fun ConfigurationModificationButtonColumn(
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context: Context = LocalContext.current

    Column(modifier = modifier) {
        ConfigurationModificationFAB(
            iconRes = R.drawable.ic_save_24,
            contentDescriptionRes = R.string.update_listener_configuration_button_cd,
            onClick = {
                // Do not sync if leading to disablement of all FileTypes
                with(mainScreenViewModel) {
                    if (accountForFileType.values.none { it }) {
                        context.showToast(R.string.all_file_types_disabled_notification)
                    } else {
                        nonAppliedListenerConfiguration
                            .launchSync()
                            .invokeOnCompletion {
                                if (mainScreenViewModel.isListenerRunning.value) {
                                    FileListenerService.reregisterMediaObservers(
                                        context
                                    )
                                }
                                context.showToast(R.string.updated_listener_configuration)
                            }
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        ConfigurationModificationFAB(
            iconRes = R.drawable.ic_reset_24,
            contentDescriptionRes = R.string.reset_button_cd,
            onClick = {
                with(mainScreenViewModel) {
                    nonAppliedListenerConfiguration.launchReset()
                }
            }
        )
    }
}

@Composable
private fun ConfigurationModificationFAB(
    @DrawableRes iconRes: Int,
    @StringRes contentDescriptionRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = contentDescriptionRes),
            modifier = Modifier.size(36.dp)
        )
    }
}