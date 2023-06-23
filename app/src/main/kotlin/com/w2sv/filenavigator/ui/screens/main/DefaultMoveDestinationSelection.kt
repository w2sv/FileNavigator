package com.w2sv.filenavigator.ui.screens.main

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anggrayudi.storage.file.getSimplePath
import com.w2sv.filenavigator.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.AppFontText
import com.w2sv.filenavigator.ui.DialogButton
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.theme.disabledColor
import com.w2sv.filenavigator.utils.toggle

@Composable
fun OpenFileSourceDefaultDestinationDialogButton(
    source: FileType.Source,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    var defaultDestinationDialogFileSource by rememberSaveable {
        mutableStateOf<FileType.Source?>(null)
    }
        .apply {
            value?.let {
                DefaultMoveDestinationDialog(
                    fileSource = it,
                    defaultMoveDestination = mainScreenViewModel.dataStoreRepository.defaultFileSourceMoveDestination.getValue(
                        source.defaultDestination
                    ).collectAsState(null).value,
                    setDefaultDestination = {
                        mainScreenViewModel.defaultDestinationPickerFileSource.value = source
                    },
                    isLocked = mainScreenViewModel.unconfirmedFileSourceDefaultDestinationLocked.getValue(
                        source.defaultDestinationLocked
                    ),
                    onLockButtonPress = {
                        mainScreenViewModel.unconfirmedFileSourceDefaultDestinationLocked.toggle(
                            source.defaultDestinationLocked
                        )
                    },
                    closeDialog = { value = null }
                )
            }
        }

    IconButton(
        onClick = { defaultDestinationDialogFileSource = source },
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_folder_settings_24),
            tint = if (enabled) MaterialTheme.colorScheme.secondary else disabledColor(),
            contentDescription = stringResource(
                R.string.open_target_directory_settings
            ),
            modifier = modifier
        )
    }
}

@Composable
private fun DefaultMoveDestinationDialog(
    fileSource: FileType.Source,
    defaultMoveDestination: Uri?,
    setDefaultDestination: () -> Unit,
    isLocked: Boolean,
    onLockButtonPress: () -> Unit,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { closeDialog() },
        confirmButton = {
            DialogButton(onClick = closeDialog) {
                AppFontText(text = stringResource(id = R.string.close))
            }
        },
        modifier = modifier,
        icon = {
            Row {
                Icon(
                    painter = painterResource(id = fileSource.fileType.iconRes),
                    contentDescription = null,
                    tint = fileSource.fileType.color,
                    modifier = Modifier.size(28.dp)
                )
                Icon(
                    painter = painterResource(id = fileSource.kind.iconRes),
                    contentDescription = null,
                    tint = fileSource.fileType.color,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            AppFontText(
                text = buildAnnotatedString {
                    append("Default ")
                    withStyle(SpanStyle(color = fileSource.fileType.color)) {
                        append(fileSource.getTitle(context))
                    }
                    append(" Move Destination")
                },
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppFontText(
                        text = defaultMoveDestination?.let {
                            DocumentFile.fromSingleUri(context, it)?.getSimplePath(context)
                        } ?: "Not set yet",
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(0.8f)
                    )
                    IconButton(onClick = setDefaultDestination, modifier = Modifier.weight(0.15f)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings_24),
                            contentDescription = stringResource(
                                R.string.change_default_move_destination
                            ),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    val lockingEnabled = defaultMoveDestination != null
                    IconButton(
                        onClick = onLockButtonPress,
                        modifier = Modifier.weight(0.15f),
                        enabled = lockingEnabled
                    ) {
                        AnimatedVisibility(visible = isLocked) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock_closed_24),
                                contentDescription = null,
                                tint = if (lockingEnabled) MaterialTheme.colorScheme.secondary else disabledColor()
                            )
                        }
                        AnimatedVisibility(visible = !isLocked) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock_open_24),
                                contentDescription = null,
                                tint = if (lockingEnabled) MaterialTheme.colorScheme.secondary else disabledColor()
                            )
                        }
                    }
                }
            }
        }
    )
}

@Preview
@Composable
private fun TargetDirectorySettingsDialogPrev() {
    AppTheme {
        DefaultMoveDestinationDialog(
            fileSource = FileType.Source(FileType.Media.Image, FileType.SourceKind.Camera),
            defaultMoveDestination = null,
            closeDialog = {},
            onLockButtonPress = {},
            isLocked = false,
            setDefaultDestination = {}
        )
    }
}