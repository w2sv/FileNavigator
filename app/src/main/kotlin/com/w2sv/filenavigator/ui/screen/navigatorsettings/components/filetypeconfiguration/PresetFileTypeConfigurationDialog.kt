package com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.common.util.colonSuffixed
import com.w2sv.composed.OnChange
import com.w2sv.domain.model.ExtensionConfigurableFileType
import com.w2sv.domain.model.NonMediaFileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarContent
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.modelext.color
import com.w2sv.filenavigator.ui.theme.dialogSectionLabel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay

@Composable
fun PresetNonMediaFileTypeConfigurationDialog(
    fileType: PresetFileType.NonMedia,
    excludedExtensions: ImmutableList<String>,
    setExcludedFileExtensions: (ExtensionConfigurableFileType, Set<String>) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mutableExcludedExtensions = remember { excludedExtensions.toMutableStateList() }
    val context = LocalContext.current

    ColorPickerDialogOverlaidFileTypeConfigurationDialog(
        fileTypeColor = fileType.color,
        applyColor = {}
    ) { openColorPickerDialog ->
        FileTypeConfigurationDialog(
            icon = fileType.iconRes,
            title = stringResource(
                R.string.preset_non_media_file_type_configuration_dialog_title,
                fileType.label(context)
            ),
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            onConfigureColorButtonPress = openColorPickerDialog,
            fileTypeColor = fileType.color,
            onConfirmButtonPress = {
                setExcludedFileExtensions(
                    fileType as ExtensionConfigurableFileType, // TODO: unsafe cast
                    mutableExcludedExtensions.toSet()
                )
            }
        ) {
            Column { // To prevent spacing in between text and chip flow row introduced by FileTypeConfigurationDialog
                Text(
                    stringResource(R.string.file_extensions).colonSuffixed(),
                    style = MaterialTheme.typography.dialogSectionLabel,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                val availableExtensions = remember {
                    when (fileType) {
                        is NonMediaFileType.WithExtensions -> fileType.fileExtensions
                        is ExtensionConfigurableFileType -> fileType.defaultFileExtensions
                        else -> error("Shouldn't happen") // TODO
                    }
                }

                var showLeaveAtLeastOneExtensionEnabledWarning by rememberSaveable { mutableStateOf(false) }
                OnChange(showLeaveAtLeastOneExtensionEnabledWarning) {
                    if (it) {
                        delay(4000L)  // Equals SnackbarDuration.Short ms
                        showLeaveAtLeastOneExtensionEnabledWarning = false
                    }
                }

                FileExtensionsChipFlowRow {
                    availableExtensions.forEach { extension ->
                        val isExcluded by remember { derivedStateOf { extension in mutableExcludedExtensions } }
                        FileExtensionChip(
                            extension = extension,
                            onClick = {
                                when {
                                    availableExtensions.size - mutableExcludedExtensions.size == 1 ->
                                        showLeaveAtLeastOneExtensionEnabledWarning =
                                            true

                                    isExcluded -> mutableExcludedExtensions.remove(extension)
                                    else -> mutableExcludedExtensions.add(extension)
                                }
                            },
                            selected = !isExcluded
                        )
                    }
                }

                AnimatedVisibility(showLeaveAtLeastOneExtensionEnabledWarning) {
                    AppSnackbarContent(
                        snackbarKind = SnackbarKind.Error,
                        message = stringResource(R.string.leave_at_least_one_file_extension_enabled),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}

@Preview
@Composable
private fun PresetNonMediaFileTypeConfigurationDialogPrev() {
    PresetNonMediaFileTypeConfigurationDialog(
        fileType = PresetFileType.Text,
        excludedExtensions = persistentListOf(),
        setExcludedFileExtensions = { _, _ -> },
        onDismissRequest = {}
    )
}
