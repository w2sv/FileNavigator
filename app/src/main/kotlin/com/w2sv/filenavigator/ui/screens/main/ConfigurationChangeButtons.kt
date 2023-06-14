package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun ListenerModificationButtons(
    parentCoroutineScope: CoroutineScope,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context: Context = LocalContext.current

    Row(modifier = modifier) {
        FloatingActionButton(
            iconRes = R.drawable.ic_check_24,
            contentDescriptionRes = R.string.update_listener_configuration_button_cd,
            onClick = {
                with(mainScreenViewModel) {
                    unconfirmedNavigatorConfiguration
                        .launchSync()
                        .invokeOnCompletion {
                            // If FileListenerService is already running, relaunch with new file observer configuration
                            if (isNavigatorRunning.value) {
                                FileNavigatorService.reregisterFileObservers(
                                    context
                                )
                            }
                            parentCoroutineScope.launch {
                                snackbarHostState.showSnackbar(
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

        Spacer(modifier = Modifier.width(10.dp))

        FloatingActionButton(
            iconRes = R.drawable.ic_reset_24,
            contentDescriptionRes = R.string.reset_button_cd,
            onClick = {
                with(mainScreenViewModel) {
                    unconfirmedNavigatorConfiguration.launchReset()
                }
            }
        )
    }
}

@Composable
private fun FloatingActionButton(
    @DrawableRes iconRes: Int,
    @StringRes contentDescriptionRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = contentDescriptionRes),
        )
    }
}