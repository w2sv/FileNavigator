package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.FileType
import com.w2sv.filenavigator.ui.AppCheckbox
import com.w2sv.filenavigator.ui.AppFontText
import com.w2sv.filenavigator.ui.ExtendedSnackbarVisuals
import com.w2sv.filenavigator.ui.SnackbarKind
import com.w2sv.filenavigator.ui.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.filenavigator.ui.theme.disabledColor
import com.w2sv.filenavigator.utils.goToManageExternalStorageSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FileTypeSelectionColumn(
    modifier: Modifier = Modifier
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

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
        ) {
            FileType.all.forEach {
                FileTypeAccordion(
                    fileType = it,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FileTypeAccordion(
    fileType: FileType,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val fileTypeEnabled = mainScreenViewModel.fileTypeStatus.getValue(fileType).isEnabled

    Column(modifier = modifier) {
        FileTypeAccordionHeader(
            fileType = fileType,
            isEnabled = fileTypeEnabled
        )
        AnimatedVisibility(visible = fileTypeEnabled) {
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
                    tint = if (isEnabled) fileType.color else disabledColor()
                )
            }
            Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.CenterStart) {
                AppFontText(
                    text = stringResource(id = fileType.titleRes),
                    fontSize = 18.sp,
                    color = if (isEnabled) Color.Unspecified else disabledColor()
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
                        when (val status = mainScreenViewModel.fileTypeStatus.getValue(fileType)) {
                            FileType.Status.Enabled, FileType.Status.Disabled -> {
                                if (mainScreenViewModel.fileTypeStatus.values.map { it.isEnabled }
                                        .atLeastOneTrueAfterValueChange(
                                            checkedNew
                                        )
                                ) {
                                    mainScreenViewModel.fileTypeStatus.toggle(fileType)
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
            ExtendedSnackbarVisuals(
                message = context.getString(
                    if (fileTypeStatus == FileType.Status.DisabledForNoFileAccess)
                        R.string.manage_external_storage_permission_rational
                    else
                        R.string.non_media_files_require_all_files_access
                ),
                kind = SnackbarKind.Error,
                actionLabel = context.getString(R.string.grant),
                action = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        goToManageExternalStorageSettings(context)
                    }
                }
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
            ExtendedSnackbarVisuals(
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

    val isEnabled = if (fileType.isMediaType) mainScreenViewModel.fileSourceEnabled.getValue(source) else true

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
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = source.kind.iconRes),
                    contentDescription = null,
                    tint = if (isEnabled) fileType.color.copy(alpha = 0.75f) else disabledColor()
                )
            }
            Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.CenterStart) {
                AppFontText(
                    text = stringResource(id = source.kind.labelRes),
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface.copy(0.7f) else disabledColor()
                )
            }
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                if (fileType.isMediaType) {
                    AppCheckbox(
                        checked = isEnabled,
                        onCheckedChange = { checkedNew ->
                            if (fileType.sources.map {
                                    mainScreenViewModel.fileSourceEnabled.getValue(
                                        it
                                    )
                                }
                                    .atLeastOneTrueAfterValueChange(checkedNew)
                            ) {
                                mainScreenViewModel.fileSourceEnabled[source] = checkedNew
                            } else {
                                scope.launch {
                                    mainScreenViewModel.snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                        ExtendedSnackbarVisuals(
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
        }
    }
}

fun Iterable<Boolean>.atLeastOneTrueAfterValueChange(newValue: Boolean): Boolean =
    newValue || count { it } > 1

/**
 * Assumes value corresponding to [key] to be one of [FileType.Status.Enabled] or [FileType.Status.Disabled].
 */
fun <K> MutableMap<K, FileType.Status>.toggle(key: K) {
    put(
        key,
        if (getValue(key) == FileType.Status.Disabled) FileType.Status.Enabled else FileType.Status.Disabled
    )
}