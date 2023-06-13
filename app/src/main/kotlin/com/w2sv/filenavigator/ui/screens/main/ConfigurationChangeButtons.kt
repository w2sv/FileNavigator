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
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.service.FileNavigatorService
import com.w2sv.filenavigator.ui.ExtendedSnackbarVisuals
import com.w2sv.filenavigator.ui.SnackbarKind

@Composable
internal fun ListenerModificationButtonColumn(
    showSnackbar: (ExtendedSnackbarVisuals) -> Unit,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context: Context = LocalContext.current

    Column(modifier = modifier) {
        ConfigurationModificationFAB(
            iconRes = R.drawable.ic_check_24,
            contentDescriptionRes = R.string.update_listener_configuration_button_cd,
            onClick = {
                // Do not sync if leading to disablement of all FileTypes
                with(mainScreenViewModel) {
                    if (accountForFileType.values.none { it }) {
                        showSnackbar(
                            ExtendedSnackbarVisuals(
                                message = context.getString(
                                    R.string.leave_at_least_one_file_type_enabled
                                ),
                                kind = SnackbarKind.Error
                            )
                        )
                    } else {
                        unconfirmedListenerConfiguration
                            .launchSync()
                            .invokeOnCompletion {
                                // If FileListenerService is already running, relaunch with new file observer configuration
                                if (isNavigatorRunning.value) {
                                    FileNavigatorService.reregisterFileObservers(
                                        context
                                    )
                                }
                                showSnackbar(
                                    ExtendedSnackbarVisuals(
                                        message = context.getString(
                                            R.string.updated_listener_configuration
                                        ),
                                        kind = SnackbarKind.Success
                                    )
                                )
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
                    unconfirmedListenerConfiguration.launchReset()
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