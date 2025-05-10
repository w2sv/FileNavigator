package com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration

import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.common.util.colonSuffixed
import com.w2sv.composed.OnChange
import com.w2sv.composed.colorSaver
import com.w2sv.composed.rememberStyledTextResource
import com.w2sv.domain.model.filetype.AnyPresetWrappingFileType
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.PresetWrappingFileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarContent
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.modelext.color
import com.w2sv.filenavigator.ui.theme.dialogSectionLabel
import com.w2sv.filenavigator.ui.util.snapshotStateListSaver
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.delay
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

private sealed interface PresetFileTypeConfigurationDialogInputWarning : Parcelable {

    val asConflictingCustomFileTypeOrNull: ConflictingCustomFileType?
        get() = this as? ConflictingCustomFileType
}

@Parcelize
private data object LastEnabledExtension : PresetFileTypeConfigurationDialogInputWarning

@Parcelize
@Stable
private data class ConflictingCustomFileType(val fileType: CustomFileType, val extension: String) :
    PresetFileTypeConfigurationDialogInputWarning {

    @IgnoredOnParcel
    val isOnlyFileTypeExtension by threadUnsafeLazy { fileType.fileExtensions.size == 1 }

    @get:Composable
    val warningText: AnnotatedString
        get() = rememberStyledTextResource(
            if (isOnlyFileTypeExtension) R.string.is_only_other_file_type_extension else R.string.is_other_file_type_extension,
            extension,
            fileType.name
        )

    @get:Composable
    val buttonText: AnnotatedString
        get() = if (isOnlyFileTypeExtension) {
            rememberStyledTextResource(
                R.string.delete_file_type,
                fileType.name
            )
        } else {
            rememberStyledTextResource(R.string.remove_from, extension, fileType.name)
        }
}

@Composable
fun PresetFileTypeConfigurationDialog(
    fileType: AnyPresetWrappingFileType,
    saveFileType: (AnyPresetWrappingFileType) -> Unit,
    customFileTypes: ImmutableSet<CustomFileType>,
    excludeFileExtension: ExcludeExtension,
    deleteCustomFileType: (CustomFileType) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var excludedExtensions = rememberSaveable(saver = snapshotStateListSaver<String>()) {
        fileType.asExtensionConfigurableTypeOrNull?.excludedExtensions?.toMutableStateList() ?: mutableStateListOf()
    }
    var color by rememberSaveable(fileType.color, stateSaver = colorSaver()) { mutableStateOf(fileType.color) }

    val editedFileType by remember {
        derivedStateOf {
            when (fileType) {
                is PresetWrappingFileType.ExtensionConfigurable -> fileType.copy(
                    excludedExtensions = excludedExtensions.toSet(),
                    colorInt = color.toArgb()
                )

                is PresetWrappingFileType.ExtensionSet -> fileType.copy(colorInt = color.toArgb())
            }
        }
    }

    val context = LocalContext.current

    ColorPickerDialogOverlaidFileTypeConfigurationDialog(
        fileTypeColor = color,
        applyColor = { color = it }
    ) { openColorPickerDialog ->
        FileTypeConfigurationDialog(
            icon = fileType.iconRes,
            title = stringResource(R.string.preset_non_media_file_type_configuration_dialog_title, fileType.label(context)),
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            onConfigureColorButtonPress = openColorPickerDialog,
            fileTypeColor = color,
            confirmButtonEnabled = remember { derivedStateOf { editedFileType != fileType } }.value,
            onConfirmButtonPress = { saveFileType(editedFileType) }
        ) {
            Column { // To prevent spacing in between text and chip flow row introduced by FileTypeConfigurationDialog
                Text(
                    text = stringResource(R.string.file_extensions).colonSuffixed(),
                    style = MaterialTheme.typography.dialogSectionLabel,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                if (fileType.isMediaType) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.media_file_type_extensions_can_t_be_modified),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                val availableExtensions: Collection<String> = remember {
                    when (fileType) {
                        is PresetWrappingFileType.ExtensionConfigurable -> fileType.defaultFileExtensions
                        is PresetWrappingFileType.ExtensionSet -> fileType.fileExtensions
                    }
                }

                var inputWarning by rememberSaveable { mutableStateOf<PresetFileTypeConfigurationDialogInputWarning?>(null) }

                OnChange(inputWarning) {
                    if (it is LastEnabledExtension) {
                        delay(4000L) // Equals duration of SnackbarDuration.Short
                        inputWarning = null
                    }
                }

                OnChange(customFileTypes) {
                    inputWarning?.asConflictingCustomFileTypeOrNull?.let { conflictingCustomFileType ->
                        if (customFileTypes
                                .firstOrNull { it.ordinal == conflictingCustomFileType.fileType.ordinal }
                                ?.fileExtensions?.contains(conflictingCustomFileType.extension) != true
                        ) {
                            excludedExtensions.remove(conflictingCustomFileType.extension)
                            inputWarning = null
                        }
                    }
                }

                FileExtensionsChipFlowRow {
                    availableExtensions.forEach { extension ->
                        val isExcluded by remember { derivedStateOf { extension in excludedExtensions } }
                        FileExtensionChip(
                            extension = extension,
                            onClick = {
                                if (inputWarning is ConflictingCustomFileType) {
                                    inputWarning = null
                                }

                                when {
                                    isExcluded -> {
                                        customFileTypes
                                            .firstOrNull { extension in it.fileExtensions }
                                            ?.let { inputWarning = ConflictingCustomFileType(it, extension) }
                                            ?: excludedExtensions.remove(extension)
                                    }

                                    availableExtensions.size - excludedExtensions.size == 1 ->
                                        inputWarning =
                                            LastEnabledExtension

                                    else -> excludedExtensions.add(extension)
                                }
                            },
                            selected = !isExcluded,
                            enabled = !fileType.isMediaType
                        )
                    }
                }

                AnimatedContent(inputWarning, modifier = Modifier.padding(top = 4.dp)) {
                    when (it) {
                        is LastEnabledExtension -> AppSnackbarContent(
                            snackbarKind = SnackbarKind.Error,
                            message = stringResource(
                                if (availableExtensions.size == 1) {
                                    R.string.extension_is_the_only_file_type_extension_and_must_not_be_disabled
                                } else {
                                    R.string.leave_at_least_one_file_extension_enabled
                                }
                            )
                        )

                        is ConflictingCustomFileType -> {
                            Column {
                                AppSnackbarContent(
                                    snackbarKind = SnackbarKind.Error,
                                    message = it.warningText
                                )
                                Spacer(Modifier.padding(vertical = 2.dp))
                                RemoveExtensionFromFileTypeButton(
                                    text = it.buttonText,
                                    onClick = {
                                        if (it.isOnlyFileTypeExtension) {
                                            deleteCustomFileType(it.fileType)
                                        } else {
                                            excludeFileExtension(it.fileType, it.extension)
                                        }
                                    }
                                )
                            }
                        }

                        null -> Unit
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}

@Preview
@Composable
private fun PresetNonMediaFileTypeConfigurationDialogPrev() {
    PresetFileTypeConfigurationDialog(
        fileType = PresetFileType.Text.toDefaultFileType(),
        saveFileType = {},
        onDismissRequest = {},
        customFileTypes = persistentSetOf(),
        excludeFileExtension = { _, _ -> },
        deleteCustomFileType = {}
    )
}
