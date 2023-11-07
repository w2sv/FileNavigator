package com.w2sv.filenavigator.ui.screens.home.components.movehistory

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.DialogButton
import com.w2sv.filenavigator.ui.sharedviewmodels.MoveHistoryViewModel

@Composable
fun MoveHistory(
    modifier: Modifier = Modifier,
    moveHistoryVM: MoveHistoryViewModel = viewModel()
) {
    val moveHistory by moveHistoryVM.moveHistory.collectAsState()

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

    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .weight(0.12f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AppFontText(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium,
                )

                AnimatedVisibility(visible = !moveHistoryIsEmpty) {
                    IconButton(
                        onClick = { showHistoryDeletionConfirmationDialog = true },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete_history_24),
                            contentDescription = "Delete move history?",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

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
                    MoveEntryColumn(history = moveHistory)
                }
            }
        }
    }
}

@Composable
private fun HistoryDeletionConfirmationDialog(closeDialog: () -> Unit, onConfirmed: () -> Unit) {
    AlertDialog(
        onDismissRequest = closeDialog,
        confirmButton = {
            DialogButton(
                text = "Yes",
                onClick = {
                    onConfirmed()
                    closeDialog()
                }
            )
        },
        dismissButton = {
            DialogButton(text = "No", onClick = closeDialog)
        },
        text = {
            AppFontText(
                text = "Delete move history?",
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
        AppFontText(
            text = "Navigated files will appear here",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}