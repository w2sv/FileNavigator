package com.w2sv.filenavigator.ui.screens.main

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.mediastore.FileType
import com.w2sv.filenavigator.ui.ExtendedSnackbarVisuals
import com.w2sv.filenavigator.ui.SnackbarKind
import com.w2sv.filenavigator.ui.showSnackbarAndDismissCurrentIfApplicable
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme
import com.w2sv.filenavigator.ui.theme.RailwayText
import com.w2sv.filenavigator.ui.theme.disabledColor
import com.w2sv.filenavigator.utils.goToManageExternalStorageSettings
import com.w2sv.filenavigator.utils.toggle
import kotlinx.coroutines.launch

@Composable
fun FileTypeSelectionColumn(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val verticalPaddingModifier = Modifier.padding(vertical = 4.dp)

        FileType.Media.all.forEach {
            FileTypeAccordion(fileType = it, modifier = verticalPaddingModifier)
        }
        NonMediaTypesHeaderRow()
        FileType.NonMedia.all.forEach {
            FileTypeAccordion(fileType = it, modifier = verticalPaddingModifier)
        }
    }
}

@Composable
private fun FileTypeAccordion(
    fileType: FileType,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    Column(modifier = modifier) {
        FileTypeAccordionHeader(fileType = fileType)

        if (fileType is FileType.Media) {
            AnimatedVisibility(visible = mainScreenViewModel.fileTypeEnabled.getValue(fileType)) {
                FileSourcesSurface(fileType = fileType)
            }
        }
    }
}

@Composable
private fun FileTypeAccordionHeader(
    fileType: FileType,
    modifier: Modifier = Modifier,
    mainScreenViewModel: MainScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isManageExternalStoragePermissionMissing =
        fileType is FileType.NonMedia && !mainScreenViewModel.manageExternalStoragePermissionGranted.collectAsState().value

    val isEnabled =
        !isManageExternalStoragePermissionMissing && mainScreenViewModel.fileTypeEnabled.getValue(
            fileType
        )

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
                RailwayText(
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
                        when (isManageExternalStoragePermissionMissing) {
                            true -> scope.launch {
                                mainScreenViewModel.snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                    ExtendedSnackbarVisuals(
                                        message = context.getString(
                                            R.string.manage_external_storage_permission_required_notification
                                        ),
                                        kind = SnackbarKind.Error,
                                        action = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                goToManageExternalStorageSettings(context)
                                            }
                                        },
                                        actionLabel = context.getString(R.string.grant)
                                    )
                                )
                            }

                            false -> {
                                if (mainScreenViewModel.fileTypeEnabled.values.atLeastOneTrueAfterValueChange(
                                        checkedNew
                                    )
                                ) {
                                    mainScreenViewModel.fileTypeEnabled.toggle(fileType)
                                } else {
                                    scope.launch {
                                        mainScreenViewModel.snackbarHostState.showSnackbarAndDismissCurrentIfApplicable(
                                            ExtendedSnackbarVisuals(
                                                message = context.getString(
                                                    R.string.leave_at_least_one_file_type_enabled
                                                ),
                                                kind = SnackbarKind.Error
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
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

    val isEnabled = mainScreenViewModel.fileSourceEnabled.getValue(source)

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
        ) {
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = source.kind.iconRes),
                    contentDescription = null,
                    tint = if (isEnabled) fileType.color.copy(alpha = 0.75f) else disabledColor()
                )
            }
            Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.CenterStart) {
                RailwayText(
                    text = stringResource(id = source.kind.labelRes),
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface.copy(0.7f) else disabledColor()
                )
            }
            Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
                Checkbox(
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary),
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

@Preview
@Composable
private fun HeaderPrev() {
    FileNavigatorTheme {
        FileTypeAccordionHeader(fileType = FileType.Image)
    }
}

fun Iterable<Boolean>.atLeastOneTrueAfterValueChange(newValue: Boolean): Boolean =
    newValue || count { it } > 1