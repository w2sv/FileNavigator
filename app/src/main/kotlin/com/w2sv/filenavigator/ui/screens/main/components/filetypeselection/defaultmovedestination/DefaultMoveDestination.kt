package com.w2sv.filenavigator.ui.screens.main.components.filetypeselection.defaultmovedestination

import android.content.Context
import android.view.animation.AnticipateOvershootInterpolator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.androidutils.coroutines.toggle
import com.w2sv.data.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.DialogButton
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.theme.DefaultIconDp
import com.w2sv.filenavigator.ui.theme.Epsilon
import com.w2sv.filenavigator.ui.utils.toEasing
import slimber.log.i

@Composable
fun OpenDefaultMoveDestinationDialogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(
                id = R.drawable.ic_folder_settings_24
            ),
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = stringResource(
                R.string.open_target_directory_settings
            ),
            modifier = Modifier.size(DefaultIconDp)
        )
    }
}

@Composable
fun DefaultMoveDestinationDialog(
    fileSource: FileType.Source,
    configuration: DefaultMoveDestinationConfiguration,
    closeDialog: () -> Unit,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
) {
    val defaultDestinationSelectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        i { "DocumentTree Uri: $treeUri" }

        configuration.onMoveDestinationSelected(treeUri, context)
    }

    val defaultMoveDestination by configuration.moveDestination.collectAsState()
    val defaultMoveDestinationIsLocked by configuration.isLocked.collectAsState()
    val configurationHasChanged by configuration.statesDissimilar.collectAsState()

    val isDestinationSet by remember {
        derivedStateOf { defaultMoveDestination != null }
    }
    var showLockInfo by rememberSaveable {
        mutableStateOf(false)
    }

    AlertDialog(properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.padding(horizontal = 32.dp),
        onDismissRequest = closeDialog,
        dismissButton = {
            DialogButton(onClick = closeDialog) {
                AppFontText(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            DialogButton(
                onClick = {
                    configuration.launchSync()
                    closeDialog()
                }, enabled = configurationHasChanged
            ) {
                AppFontText(text = stringResource(id = R.string.apply))
            }
        },
        icon = {
            Row {
                Icon(
                    painter = painterResource(id = fileSource.fileType.iconRes),
                    contentDescription = null,
                    tint = fileSource.fileType.color,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
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
                }, textAlign = TextAlign.Center, fontSize = 18.sp
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppFontText(text = defaultMoveDestination?.let {
                        DocumentFile.fromSingleUri(context, it)?.getSimplePath(context)
                    } ?: stringResource(R.string.not_set),
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(0.8f),
                        color = if (isDestinationSet) Color.Unspecified else AppColor.disabled)

                    val buttonBoxWeight = 0.15f
                    val isDestinationSetDependentBoxWeight by animateFloatAsState(
                        targetValue = if (isDestinationSet) buttonBoxWeight - Epsilon else Epsilon,
                        animationSpec = tween(
                            durationMillis = DefaultAnimationDuration,
                            delayMillis = if (isDestinationSet) 150 else 0,
                            easing = AnticipateOvershootInterpolator().toEasing()
                        ),
                        label = ""
                    )
                    Spacer(
                        modifier = Modifier.weight(
                            maxOf(
                                (buttonBoxWeight - isDestinationSetDependentBoxWeight) * 2,
                                Epsilon
                            )
                        )
                    )
                    // Pick button
                    IconButton(
                        onClick = {
                            defaultDestinationSelectionLauncher.launch(null)  // TODO: Pass start dir treeUri
                        },
                        modifier = Modifier.weight(buttonBoxWeight)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_folder_open_24),
                            contentDescription = stringResource(
                                R.string.change_default_move_destination
                            ),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    // Lock button
                    IconButton(
                        onClick = {
                            configuration.isLocked.toggle()
                            showLockInfo = true
                        },
                        modifier = Modifier.weight(
                            maxOf(
                                isDestinationSetDependentBoxWeight,
                                Epsilon
                            )
                        ),
                    ) {
                        AnimatedContent(
                            targetState = defaultMoveDestinationIsLocked,
                            label = "",
                            transitionSpec = {
                                (slideInHorizontally() + fadeIn()).togetherWith(
                                    slideOutHorizontally() + fadeOut()
                                )
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = if (it) R.drawable.ic_lock_closed_24 else R.drawable.ic_lock_open_24),
                                contentDescription = stringResource(if (it) R.string.unlock_default_move_destination else R.string.unlock_default_move_destination),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    // Delete button
                    IconButton(
                        onClick = {
                            configuration.moveDestination.value = null
                            configuration.isLocked.value = false
                        },
                        modifier = Modifier.weight(
                            maxOf(
                                isDestinationSetDependentBoxWeight,
                                Epsilon
                            )
                        ),
                        enabled = isDestinationSet
                    ) {
                        Icon(
                            painter = painterResource(id = com.w2sv.navigator.R.drawable.ic_delete_24),
                            contentDescription = stringResource(R.string.delete_default_move_destination),
                            tint = if (isDestinationSet) MaterialTheme.colorScheme.secondary else AppColor.disabled
                        )
                    }
                }
                AnimatedVisibility(visible = showLockInfo) {
                    AppFontText(
                        text = stringResource(if (defaultMoveDestinationIsLocked) R.string.default_move_destination_locked_info else R.string.default_move_destination_unlocked_info),
                        color = AppColor.disabled
                    )
                    LaunchedEffect(key1 = defaultMoveDestinationIsLocked) {
                        launchDelayed(5000L) {
                            showLockInfo = false
                        }
                    }
                }
            }
        }
    )
}

//@Preview
//@Composable
//private fun DefaultMoveDestinationDialogPrev() {
//    AppTheme {
//        DefaultMoveDestinationDialog(
//            fileSource = FileType.Source(FileType.Media.Image, FileType.SourceKind.Camera),
//            defaultMoveDestination = null,
//            closeDialog = {},
//            setDefaultDestination = {},
//            resetDefaultDestination = {},
//            unconfirmedDefaultMoveDestinationState = UnconfirmedStateFlow()
//        )
//    }
//}