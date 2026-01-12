package com.w2sv.filenavigator.ui.screen.home.movehistory

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.core.common.R
import com.w2sv.designsystem.DialogButton
import com.w2sv.designsystem.theme.onSurfaceVariantDecreasedAlpha
import com.w2sv.filenavigator.ui.designsystem.MoreIconButtonWithDropdownMenu
import com.w2sv.filenavigator.ui.screen.home.HomeScreenCard

@Composable
fun MoveHistoryCard(state: MoveHistoryState, modifier: Modifier = Modifier) {
    var showHistoryDeletionDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showHistoryDeletionDialog) {
        HistoryDeletionDialog(
            closeDialog = { showHistoryDeletionDialog = false },
            onConfirmed = state.deleteAll
        )
    }

    MoveHistoryCard(
        state = state,
        showDeletionDialog = { showHistoryDeletionDialog = true },
        modifier = modifier
    )
}

@Composable
private fun MoveHistoryCard(state: MoveHistoryState, showDeletionDialog: () -> Unit, modifier: Modifier = Modifier) {
    HomeScreenCard(modifier) {
        HeaderRow(
            showDropdownMenuButton = !state.historyEmpty,
            showDeletionDialog = showDeletionDialog,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = state.historyEmpty,
            modifier = Modifier
                .weight(0.8f)
                .fillMaxWidth()
        ) {
            if (it) {
                NoHistoryPlaceHolder()
            } else {
                MoveHistory(state = state)
            }
        }
    }
}

@Composable
private fun HeaderRow(showDropdownMenuButton: Boolean, showDeletionDialog: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.move_history),
            style = MaterialTheme.typography.headlineMedium
        )

        AnimatedVisibility(visible = showDropdownMenuButton) {
            MoreIconButtonWithDropdownMenu {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete_history_24),
                            contentDescription = null
                        )
                    },
                    text = { Text(stringResource(R.string.delete_move_history_dropdown_menu_item_label)) },
                    onClick = {
                        collapseMenu()
                        showDeletionDialog()
                    }
                )
            }
        }
    }
}

@Composable
private fun HistoryDeletionDialog(closeDialog: () -> Unit, onConfirmed: () -> Unit) {
    AlertDialog(
        onDismissRequest = closeDialog,
        confirmButton = {
            DialogButton(
                text = stringResource(id = R.string.yes),
                onClick = {
                    onConfirmed()
                    closeDialog()
                }
            )
        },
        dismissButton = { DialogButton(text = stringResource(id = R.string.no), onClick = closeDialog) },
        text = { Text(text = stringResource(R.string.move_history_deletion_dialog_message)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete_history_24),
                contentDescription = null
            )
        }
    )
}

@Composable
private fun NoHistoryPlaceHolder(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalContentColor provides colorScheme.onSurfaceVariantDecreasedAlpha) {
            Icon(
                painter = painterResource(id = R.drawable.ic_history_24),
                contentDescription = null,
                modifier = Modifier.size(92.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.move_history_placeholder),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
