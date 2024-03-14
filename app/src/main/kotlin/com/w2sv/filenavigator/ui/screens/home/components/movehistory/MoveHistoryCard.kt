package com.w2sv.filenavigator.ui.screens.home.components.movehistory

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.composed.CollectLatestFromFlow
import com.w2sv.composed.extensions.dismissCurrentSnackbarAndShow
import com.w2sv.domain.model.MoveEntry
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.designsystem.LocalSnackbarHostState
import com.w2sv.filenavigator.ui.designsystem.MoreElevatedCard
import com.w2sv.filenavigator.ui.designsystem.SnackbarAction
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.sharedviewmodels.FileRetrievalResult
import com.w2sv.filenavigator.ui.sharedviewmodels.MoveHistoryViewModel
import kotlinx.collections.immutable.toImmutableList

@Composable
fun MoveHistoryCard(
    modifier: Modifier = Modifier,
    moveHistoryVM: MoveHistoryViewModel = viewModel()
) {
    val moveHistory by moveHistoryVM.moveHistory.collectAsStateWithLifecycle()

    val moveHistoryIsEmpty by remember {
        derivedStateOf { moveHistory.isEmpty() }
    }
    var showHistoryDeletionConfirmationDialog by remember {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                HistoryDeletionConfirmationDialog(
                    closeDialog = { value = false },
                    onConfirmed = moveHistoryVM::launchHistoryDeletion
                )
            }
        }
    val retrieveAndViewFile: (MoveEntry) -> Unit = rememberRetrieveAndViewFile()

    MoreElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .weight(0.12f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.history),
                    style = MaterialTheme.typography.headlineMedium,
                )

                AnimatedVisibility(visible = !moveHistoryIsEmpty) {
                    IconButton(
                        onClick = { showHistoryDeletionConfirmationDialog = true },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete_history_24),
                            contentDescription = stringResource(R.string.delete_move_history),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedContent(
                targetState = moveHistoryIsEmpty,
                label = "",
                modifier = Modifier
                    .weight(0.8f)
                    .fillMaxWidth()
            ) {
                if (it) {
                    NoHistoryPlaceHolder()
                } else {
                    MoveEntryColumn(
                        history = moveHistory.toImmutableList(),
                        onRowClick = retrieveAndViewFile,
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberRetrieveAndViewFile(
    moveHistoryVM: MoveHistoryViewModel = viewModel(),
    context: Context = LocalContext.current,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current
): (MoveEntry) -> Unit {
    CollectLatestFromFlow(
        flow = moveHistoryVM.fileRetrievalResult,
        action = remember {
            { result ->
                when (result) {
                    is FileRetrievalResult.CouldntFindFile -> {
                        snackbarHostState.dismissCurrentSnackbarAndShow(
                            AppSnackbarVisuals(
                                message = context.getString(R.string.couldn_t_find_file),
                                kind = SnackbarKind.Error,
                                action = SnackbarAction(
                                    label = context.getString(R.string.delete_entry),
                                    callback = {
                                        moveHistoryVM.launchEntryDeletion(result.moveEntry)
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                    }
                                )
                            )
                        )
                    }

                    is FileRetrievalResult.Success -> {
                        context.startActivity(
                            Intent()
                                .setAction(Intent.ACTION_VIEW)
                                .setDataAndType(
                                    result.mediaUri,
                                    result.moveEntry.fileType.simpleStorageMediaType.mimeType
                                )
                        )
                    }
                }
            }
        }
    )

    return remember {
        { moveEntry ->
            moveHistoryVM.launchFileRetrieval(
                moveEntry = moveEntry,
                context = context
            )
        }
    }
}

@Composable
private fun HistoryDeletionConfirmationDialog(closeDialog: () -> Unit, onConfirmed: () -> Unit) {
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
        dismissButton = {
            DialogButton(text = stringResource(id = R.string.no), onClick = closeDialog)
        },
        text = {
            Text(
                text = stringResource(R.string.delete_move_history),
                textAlign = TextAlign.Center
            )
        },
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
        Icon(
            painter = painterResource(id = R.drawable.ic_history_24),
            contentDescription = null,
            modifier = Modifier.size(92.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.navigated_files_will_appear_here),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}