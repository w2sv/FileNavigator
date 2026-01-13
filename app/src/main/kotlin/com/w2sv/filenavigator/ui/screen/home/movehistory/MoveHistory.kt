package com.w2sv.filenavigator.ui.screen.home.movehistory

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.filenavigator.ui.LocalMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarAction
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.designsystem.WeightedBox
import com.w2sv.filenavigator.ui.modelext.color
import com.w2sv.filenavigator.ui.modelext.exists
import com.w2sv.filenavigator.ui.modelext.startViewFileActivity
import com.w2sv.filenavigator.ui.util.WithLocalContentColor
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
            // Use local scope so that launched snackbar disappears automatically when respective FileMoveSummary was scrolled off screen
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

/**
 * Remembers [MovedFile.exists] and updates the value on [Lifecycle.Event.ON_START], as file might have been deleted while the app was in
 * the background.
 */
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
        WithLocalContentColor(colorScheme.onSecondaryContainer) {
            WeightedBox(weight = 0.2f, contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(0.5f)) {
            Text(
                text = destinationName,
                fontSize = MoveHistoryDefaults.FontSize
            )
            if (movedFile.autoMoved) {
                Text(
                    stringResource(R.string.automoved_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .border(1.dp, colorScheme.secondary, shapes.extraLarge)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
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
                    .width(24.dp)
                    .height(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    painter = painterResource(id = moveEntry.fileAndSourceType.iconRes),
                    contentDescription = null,
                    tint = moveEntry.fileType.color
                )
            }
        }
    }
}

// @Preview
// @Composable
// private fun Prev() {
//    AppTheme {
//        FileMoveSummary(
//            movedFile = MovedFile.External(
//                name = "some file name",
//                originalName = null,
//                fileType = PresetFileType.Image.toDefaultFileType(),
//                sourceType = SourceType.Screenshot,
//                moveDestination = object : ExternalDestinationApi {
//                    override val providerAppLabel: String = ""
//                    override val providerPackageName: String = ""
//                    override val documentUri: DocumentUri = DocumentUri.parse("")
//                    override fun fileName(context: Context): String =
//                        ""
//
//                    override fun uiRepresentation(context: Context): String =
//                        ""
//                },
//                moveDateTime = LocalDateTime.now()
//            ),
//            movedFileExists = true,
//            destinationName = "some destination",
//            onClick = {}
//        )
//    }
// }

@Preview
@Composable
private fun LongNamePrev() {
    Prev("Screenshot_2925_91_09-435-67-2-5_com.w2sv.filenavigator.jpg", useDynamicColor = true)
}

@Preview
@Composable
private fun ShortNamePrevDynamic() {
    Prev("Image.jpg", useDynamicColor = false)
}

@Preview
@Composable
private fun ShortNamePrevDynamicDark() {
    Prev("Image.jpg", useDynamicColor = true, useDarkTheme = true)
}

@Composable
private fun Prev(fileName: String, useDynamicColor: Boolean, useDarkTheme: Boolean = false) {
    AppTheme(useDynamicColors = useDynamicColor, useDarkTheme = useDarkTheme) {
        FileMoveSummary(
            movedFile = MovedFile.Local(
                documentUri = DocumentUri.parse(""),
                mediaUri = null,
                name = fileName,
                originalName = null,
                fileType = PresetFileType.Image.toDefaultFileType(),
                sourceType = SourceType.Screenshot,
                moveDestination = object : LocalDestinationApi {
                    override fun pathRepresentation(context: Context, includeVolumeName: Boolean): String =
                        ""
                    override val isVolumeRoot: Boolean = true
                    override val documentUri: DocumentUri = DocumentUri.parse("")
                    override fun fileName(context: Context): String =
                        ""

                    override fun uiRepresentation(context: Context): String =
                        ""
                },
                moveDateTime = LocalDateTime.now(),
                autoMoved = true
            ),
            movedFileExists = true,
            destinationName = "some destination",
            onClick = {}
        )
    }
}
