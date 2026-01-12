package com.w2sv.filenavigator.ui.screen.home.movehistory

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.w2sv.common.uri.DocumentUri
import com.w2sv.composed.core.OnLifecycleEvent
import com.w2sv.composed.core.extensions.thenIf
import com.w2sv.core.common.R
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.ExternalDestinationApi
import com.w2sv.filenavigator.ui.LocalMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarAction
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.designsystem.WeightedBox
import com.w2sv.filenavigator.ui.modelext.color
import com.w2sv.filenavigator.ui.modelext.exists
import com.w2sv.filenavigator.ui.modelext.startViewFileActivity
import com.w2sv.filenavigator.ui.util.snackbar.SnackbarController
import com.w2sv.filenavigator.ui.util.snackbar.rememberSnackbarController
import eu.wewox.textflow.material3.TextFlow
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private object MoveHistoryDefaults {
    val FontSize = 14.sp
}

@Composable
fun MoveHistory(state: MoveHistoryState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val indexToDate = rememberIndexToDateLabel(state.history, context)
    val snackbarController = rememberSnackbarController()
    val moveDestinationPathConverter = LocalMoveDestinationPathConverter.current

    LazyColumn(modifier = modifier) {
        itemsIndexed(state.history, key = { _, movedFile -> movedFile.moveDateTime }) { i, movedFile ->
            indexToDate[i]?.let { dateRepresentation ->
                Text(
                    text = dateRepresentation,
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            val movedFileExists = rememberUpdatedMovedFileExists(movedFile = movedFile, context = context)
            // Use local scope so that launched snackbars disappear automatically when respective summaries are scrolled off screen
            val scope = rememberCoroutineScope()
            FileMoveSummary(
                movedFile = movedFile,
                movedFileExists = movedFileExists,
                destinationName = remember(movedFile) { moveDestinationPathConverter(movedFile.moveDestination, context) },
                onClick = {
                    onSummaryClick(
                        movedFile = movedFile,
                        movedFileExists = movedFileExists,
                        launchHistoryEntryDeletion = state.deleteEntry,
                        scope = scope,
                        context = context,
                        snackbarController = snackbarController
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun rememberUpdatedMovedFileExists(movedFile: MovedFile, context: Context = LocalContext.current): Boolean {
    var movedFileExists by remember(movedFile) {
        mutableStateOf(movedFile.exists(context))
    }
    OnLifecycleEvent(lifecycleEvent = Lifecycle.Event.ON_START, key1 = movedFile) {
        if (movedFileExists) {
            movedFileExists = movedFile.exists(context)
        }
    }
    return movedFileExists
}

// Using scope parameter instead of suspending function since a scope is required for the nested snackbar launching onError anyways
private fun onSummaryClick(
    scope: CoroutineScope,
    movedFile: MovedFile,
    movedFileExists: Boolean,
    launchHistoryEntryDeletion: (MovedFile) -> Unit,
    context: Context,
    snackbarController: SnackbarController
) {
    scope.launch {
        if (movedFileExists) {
            snackbarController.dismissCurrent()
            movedFile.startViewFileActivity(
                context = context,
                onError = { e ->
                    scope.launch {
                        snackbarController.show {
                            AppSnackbarVisuals(
                                message = getString(
                                    when (e) {
                                        is ActivityNotFoundException -> R.string.provider_does_not_support_file_viewing
                                        else -> R.string.can_t_view_file_from_within_file_navigator
                                    }
                                ),
                                kind = SnackbarKind.Error
                            )
                        }
                    }
                }
            )
        } else {
            snackbarController.showReplacing {
                AppSnackbarVisuals(
                    message = getString(R.string.couldn_t_find_file),
                    kind = SnackbarKind.Error,
                    action = SnackbarAction(
                        label = getString(R.string.delete_entry),
                        callback = {
                            launchHistoryEntryDeletion(movedFile)
                            snackbarController.dismissCurrent()
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun FileMoveSummary(
    movedFile: MovedFile,
    movedFileExists: Boolean,
    destinationName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shapes.medium)
            .clickable(onClick = onClick)
            .background(color = colorScheme.secondaryContainer)
            .thenIf(!movedFileExists) { alpha(0.32f) }
            .padding(8.dp)
    ) {
        FileNameWithTypeAndSourceIcon(moveEntry = movedFile, modifier = Modifier.weight(0.7f))
        CompositionLocalProvider(value = LocalContentColor provides colorScheme.primary) {
            WeightedBox(weight = 0.2f, contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        WeightedBox(weight = 0.5f) {
            Text(
                text = destinationName,
                fontSize = MoveHistoryDefaults.FontSize
            )
        }
    }
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        FileMoveSummary(
            movedFile = MovedFile.External(
                name = "some file name",
                originalName = null,
                fileType = PresetFileType.Image.toDefaultFileType(),
                sourceType = SourceType.Screenshot,
                moveDestination = object : ExternalDestinationApi {
                    override val providerAppLabel: String = ""
                    override val providerPackageName: String = ""
                    override val documentUri: DocumentUri = DocumentUri.parse("")
                    override fun fileName(context: Context): String =
                        ""
                    override fun uiRepresentation(context: Context): String =
                        ""
                },
                moveDateTime = LocalDateTime.now()
            ),
            movedFileExists = true,
            destinationName = "some destination",
            onClick = {}
        )
    }
}

@Composable
private fun FileNameWithTypeAndSourceIcon(moveEntry: MovedFile, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        TextFlow(
            text = moveEntry.name,
            fontSize = MoveHistoryDefaults.FontSize
        ) {
            // Placeholder that determines the obstacle size
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(20.dp)
            )
        }
        Icon(
            painter = painterResource(id = moveEntry.fileAndSourceType.iconRes),
            contentDescription = null,
            tint = moveEntry.fileType.color
        )
    }
}
