package com.w2sv.filenavigator.ui.screens.main.components

import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.common.utils.goToManageExternalStorageSettings
import com.w2sv.data.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppCheckbox
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.SnackbarAction
import com.w2sv.filenavigator.ui.components.SnackbarKind
import com.w2sv.filenavigator.ui.components.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.screens.main.MainScreenViewModel
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.theme.Epsilon
import com.w2sv.filenavigator.ui.utils.allFalseAfterEnteringValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTypeSelectionColumn(
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
    ) {
        AppFontText(
            text = stringResource(id = R.string.navigated_file_types),
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        val animatedFileTypes = mutableSetOf<FileType>()
        var nRunningAnimations = remember {
            0
        }

        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()

//        LaunchedEffect(Unit) {
//            mainScreenViewModel.fileTypeStatusHasChanged.collect {
//                if (it) {
//                    listState.animateScrollToItem(0)
//                }
//                mainScreenViewModel.fileTypeStatusHasChanged.emit(false)
//            }
//        }

        LazyColumn(
            state = listState
        ) {
            items(mainScreenViewModel.sortedFileTypes, key = { it }) { fileType ->
                i { "Laying out ${fileType.identifier}" }

                FileTypeAccordion(
                    fileType = fileType,
                    animate = !animatedFileTypes.contains(fileType),
                    nRunningAnimations = nRunningAnimations,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItemPlacement(
                            tween(durationMillis = DefaultAnimationDuration)
                        )
                )
                if (animatedFileTypes.add(fileType)) {
                    nRunningAnimations += 1
                    scope.launchDelayed(250L) {
                        nRunningAnimations -= 1
                    }
                }
            }
        }
    }
}

@Composable
private fun FileTypeAccordion(
    fileType: FileType,
    animate: Boolean,
    nRunningAnimations: Int,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val fileTypeEnabled =
        mainScreenViewModel.unconfirmedFileTypeStatus.getValue(fileType.status).isEnabled

    var animatedProgress by remember { mutableFloatStateOf(if (animate) 0f else 1f) }

    if (animate) {
        LaunchedEffect(key1 = fileType) {
            animatedProgress = 1f
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 500, delayMillis = 150 + nRunningAnimations * 100),
        label = ""
    )

    Column(
        modifier = modifier.graphicsLayer(
            alpha = animatedAlpha,
            scaleX = animatedAlpha,
            scaleY = animatedAlpha
        )
    ) {
        FileTypeAccordionHeader(
            fileType = fileType,
            isEnabled = fileTypeEnabled
        )
        AnimatedVisibility(
            visible = fileTypeEnabled,
            enter = expandVertically(animationSpec = spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(Spring.DampingRatioMediumBouncy)) + fadeOut()
        ) {
            FileSourcesSurface(fileType = fileType)
        }
    }
}

@Composable
private fun FileTypeAccordionHeader(
    fileType: FileType,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(tonalElevation = 2.dp, shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = fileType.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = if (isEnabled) fileType.color else AppColor.disabled
                )
            }
            Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.CenterStart) {
                AppFontText(
                    text = stringResource(id = fileType.titleRes),
                    fontSize = 18.sp,
                    color = if (isEnabled) Color.Unspecified else AppColor.disabled
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Switch(
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(8.dp),
                    checked = isEnabled,
                    onCheckedChange = { checkedNew ->
                        when (val status =
                            mainScreenViewModel.unconfirmedFileTypeStatus.getValue(fileType.status)) {
                            FileType.Status.Enabled, FileType.Status.Disabled -> {
                                if (!mainScreenViewModel.unconfirmedFileTypeStatus.values.map { it.isEnabled }
                                        .allFalseAfterEnteringValue(
                                            checkedNew
                                        )
                                ) {
                                    mainScreenViewModel.unconfirmedFileTypeStatus.toggle(fileType.status)
                                } else {
                                    scope.showLeaveAtLeastOneFileTypeEnabledSnackbar(
                                        mainScreenViewModel.snackbarHostState,
                                        context
                                    )
                                }
                            }

                            FileType.Status.DisabledForNoFileAccess, FileType.Status.DisabledForMediaAccessOnly -> scope.showManageExternalStorageSnackbar(
                                status,
                                mainScreenViewModel.snackbarHostState,
                                context
                            )
                        }
                    }
                )
            }
        }
    }
}

/**
 * Assumes [fileTypeStatus] to be one of [FileType.Status.DisabledForNoFileAccess], [FileType.Status.DisabledForMediaAccessOnly].
 */
private fun CoroutineScope.showManageExternalStorageSnackbar(
    fileTypeStatus: FileType.Status,
    snackbarHostState: SnackbarHostState,
    context: Context
) {
    launch {
        snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
            AppSnackbarVisuals(
                message = context.getString(
                    if (fileTypeStatus == FileType.Status.DisabledForNoFileAccess)
                        R.string.manage_external_storage_permission_rational
                    else
                        R.string.non_media_files_require_all_files_access
                ),
                kind = SnackbarKind.Error,
                action = SnackbarAction(
                    label = context.getString(R.string.grant),
                    callback = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            goToManageExternalStorageSettings(context)
                        }
                    }
                )
            )
        )
    }
}

private fun CoroutineScope.showLeaveAtLeastOneFileTypeEnabledSnackbar(
    snackbarHostState: SnackbarHostState,
    context: Context
) {
    launch {
        snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
            AppSnackbarVisuals(
                message = context.getString(
                    R.string.leave_at_least_one_file_type_enabled
                ),
                kind = SnackbarKind.Error
            )
        )
    }
}

@Composable
private fun FileSourcesSurface(
    fileType: FileType,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            fileType.sources.forEachIndexed { i, origin ->
                FileSourceRow(fileType = fileType, source = origin)
                if (i != fileType.sources.lastIndex) {
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun FileSourceRow(
    fileType: FileType,
    source: FileType.Source,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isEnabled =
        if (fileType.isMediaType) mainScreenViewModel.unconfirmedFileSourceEnablement.getValue(
            source.isEnabled
        ) else true

    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            // Source icon
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = source.kind.iconRes),
                    contentDescription = null,
                    tint = if (isEnabled) fileType.color.copy(alpha = 0.75f) else AppColor.disabled
                )
            }
            // Source label
            Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.CenterStart) {
                AppFontText(
                    text = stringResource(id = source.kind.labelRes),
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface.copy(0.7f) else AppColor.disabled
                )
            }

            val buttonBoxWeight = 0.1f
            val destinationButtonBoxWeight by animateFloatAsState(
                targetValue = if (isEnabled) buttonBoxWeight else Epsilon,
                label = ""
            )

            // Empty box, pushing the checkbox into the position of the destinationButtonBox upon vanishing of the latter
            Spacer(modifier = Modifier.weight(buttonBoxWeight - destinationButtonBoxWeight + Epsilon))
            // CheckboxContent
            Box(modifier = Modifier.weight(buttonBoxWeight), contentAlignment = Alignment.Center) {
                if (fileType.isMediaType) {
                    AppCheckbox(
                        checked = isEnabled,
                        onCheckedChange = { checkedNew ->
                            if (!fileType.sources.map {
                                    mainScreenViewModel.unconfirmedFileSourceEnablement.getValue(
                                        it.isEnabled
                                    )
                                }
                                    .allFalseAfterEnteringValue(checkedNew)
                            ) {
                                mainScreenViewModel.unconfirmedFileSourceEnablement[source.isEnabled] =
                                    checkedNew
                            } else {
                                scope.launch {
                                    mainScreenViewModel.snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                        AppSnackbarVisuals(
                                            message = context.getString(R.string.leave_at_least_one_file_source_selected_or_disable_the_entire_file_type),
                                            kind = SnackbarKind.Error
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Destination Button
            Box(
                modifier = Modifier
                    .weight(destinationButtonBoxWeight)
                    .alpha(destinationButtonBoxWeight * 10),
                contentAlignment = Alignment.Center
            ) {
                OpenFileSourceDefaultDestinationDialogButton(
                    source = source
                )
            }
        }
    }
}

/**
 * Assumes value corresponding to [key] to be one of [FileType.Status.Enabled] or [FileType.Status.Disabled].
 */
fun <K> MutableMap<K, FileType.Status>.toggle(key: K) {
    put(
        key,
        if (getValue(key) == FileType.Status.Disabled) FileType.Status.Enabled else FileType.Status.Disabled
    )
}