package com.w2sv.filenavigator.ui.screen.home.movehistory

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.designsystem.component.AppCard
import com.w2sv.designsystem.component.AppCardHeaderIcon
import com.w2sv.designsystem.component.DialogButton
import com.w2sv.designsystem.component.MoreIconButtonWithDropdownMenu
import com.w2sv.filenavigator.ui.shared.debugging.PreviewOf
import com.w2sv.modules.resources.R
import kotlinx.collections.immutable.persistentListOf

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

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        MoveHistoryCard(MoveHistoryState(persistentListOf(), {}, {}))
    }
}

@Composable
private fun MoveHistoryCard(state: MoveHistoryState, showDeletionDialog: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        title = stringResource(R.string.move_history),
        headerIcon = { AppCardHeaderIcon(R.drawable.ic_history_24) },
        modifier = modifier,
        trailingHeaderContent = {
            Spacer(modifier = Modifier.weight(1f))
            HeaderMenu(
                showDropdownMenuButton = !state.historyEmpty,
                showDeletionDialog = showDeletionDialog
            )
        }
    ) {
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
private fun HeaderMenu(showDropdownMenuButton: Boolean, showDeletionDialog: () -> Unit) {
    AnimatedVisibility(visible = showDropdownMenuButton) {
        MoreIconButtonWithDropdownMenu(iconTint = colorScheme.secondary) {
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
                },
                contentColor = colorScheme.onErrorContainer,
                containerColor = colorScheme.errorContainer
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
        Image(
            painter = painterResource(id = R.drawable.move_history_decorator),
            contentDescription = null,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = stringResource(R.string.move_history_placeholder),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
