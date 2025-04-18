package com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.core.domain.R
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.filenavigator.ui.designsystem.DeletionTooltip
import com.w2sv.filenavigator.ui.designsystem.OutlinedTextField
import com.w2sv.filenavigator.ui.modelext.color
import com.w2sv.filenavigator.ui.modelext.stringResource
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.util.ClearFocusOnFlowEmissionOrKeyboardHidden
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.launch

typealias ExcludeExtension = (FileType, String) -> Unit

@Composable
fun CustomFileTypeCreationDialog(
    fileTypes: ImmutableSet<FileType>,
    onDismissRequest: () -> Unit,
    createFileType: (CustomFileType) -> Unit,
    excludeFileExtension: ExcludeExtension,
    modifier: Modifier = Modifier
) {
    ColorPickerDialogOverlaidCustomFileTypeConfigurationDialog(
        title = stringResource(com.w2sv.filenavigator.R.string.create_file_type_dialog_title),
        confirmButtonText = stringResource(com.w2sv.filenavigator.R.string.create),
        customFileTypeEditor = rememberCustomFileTypeEditor(fileTypes, createFileType),
        onDismissRequest = onDismissRequest,
        excludeFileExtension = excludeFileExtension,
        modifier = modifier
    )
}

@Composable
fun CustomFileTypeConfigurationDialog(
    fileType: CustomFileType,
    fileTypes: ImmutableSet<FileType>,
    onDismissRequest: () -> Unit,
    saveFileType: (CustomFileType) -> Unit,
    excludeFileExtension: ExcludeExtension,
    modifier: Modifier = Modifier
) {
    ColorPickerDialogOverlaidCustomFileTypeConfigurationDialog(
        title = stringResource(com.w2sv.filenavigator.R.string.edit_file_type_dialog_title),
        confirmButtonText = stringResource(com.w2sv.filenavigator.R.string.save),
        customFileTypeEditor = rememberCustomFileTypeEditor(fileTypes, saveFileType, fileType),
        onDismissRequest = onDismissRequest,
        excludeFileExtension = excludeFileExtension,
        modifier = modifier
    )
}

@Composable
private fun ColorPickerDialogOverlaidCustomFileTypeConfigurationDialog(
    title: String,
    confirmButtonText: String,
    customFileTypeEditor: CustomFileTypeEditor,
    onDismissRequest: () -> Unit,
    excludeFileExtension: ExcludeExtension,
    modifier: Modifier = Modifier
) {
    ColorPickerDialogOverlaidFileTypeConfigurationDialog(
        fileTypeColor = customFileTypeEditor.fileType.color,
        applyColor = customFileTypeEditor::updateColor
    ) { openColorPickerDialog ->
        StatelessCustomFileTypeConfigurationDialog(
            title = title,
            confirmButtonText = confirmButtonText,
            customFileTypeEditor = customFileTypeEditor,
            onDismissRequest = onDismissRequest,
            onConfigureColorButtonPress = openColorPickerDialog,
            excludeFileExtension = excludeFileExtension,
            modifier = modifier
        )
    }
}

@Composable
private fun StatelessCustomFileTypeConfigurationDialog(
    title: String,
    confirmButtonText: String,
    customFileTypeEditor: CustomFileTypeEditor,
    onDismissRequest: () -> Unit,
    onConfigureColorButtonPress: () -> Unit,
    excludeFileExtension: ExcludeExtension,
    modifier: Modifier = Modifier
) {
    FileTypeConfigurationDialog(
        icon = R.drawable.ic_custom_file_type_24,
        title = title,
        confirmButtonText = confirmButtonText,
        confirmButtonEnabled = customFileTypeEditor.canBeCreated,
        onConfirmButtonPress = customFileTypeEditor::create,
        onDismissRequest = onDismissRequest,
        fileTypeColor = customFileTypeEditor.fileType.color,
        onConfigureColorButtonPress = onConfigureColorButtonPress,
        modifier = modifier.pointerInput(Unit) { detectTapGestures { customFileTypeEditor.clearFocus() } }
    ) {
        ClearFocusOnFlowEmissionOrKeyboardHidden(customFileTypeEditor.clearFocus)

        ValidityIndicatingArea(customFileTypeEditor.nameEditor.isValid) {
            OutlinedTextField(
                editor = customFileTypeEditor.nameEditor,
                placeholderText = stringResource(com.w2sv.filenavigator.R.string.edit_file_type_name_field_placeholder),
                labelText = stringResource(com.w2sv.filenavigator.R.string.edit_file_type_name_field_label),
                onApply = customFileTypeEditor::clearFocus
            )
        }
        ValidityIndicatingArea(
            customFileTypeEditor.fileType.fileExtensions.isNotEmpty(),
            modifier = Modifier.animateContentSize()
        ) {
            Column {
                OutlinedTextField(
                    editor = customFileTypeEditor.extensionEditor,
                    placeholderText = stringResource(com.w2sv.filenavigator.R.string.add_file_extension_field_placeholder),
                    labelText = stringResource(com.w2sv.filenavigator.R.string.file_extension),
                    onApply = customFileTypeEditor::addExtension,
                    applyIconImageVector = Icons.Outlined.Add,
                    showApplyIconOnlyWhenFocused = false,
                    showDisabledApplyButtonWhenEmpty = true,
                    actionButton = customFileTypeEditor.extensionEditor.invalidityReason?.isExcludableFileTypeExtensionOrNull
                        ?.let { excludableFileTypeExtension ->
                            {
                                FilledTonalButton(
                                    onClick = {
                                        excludeFileExtension(
                                            excludableFileTypeExtension.fileType,
                                            excludableFileTypeExtension.fileExtension
                                        )
                                    }
                                ) { Text("Exclude from ${excludableFileTypeExtension.fileType.stringResource()}") }
                            }
                        }
                )
                Spacer(Modifier.height(4.dp))
                if (customFileTypeEditor.fileType.fileExtensions.isNotEmpty()) {
                    FileExtensionsChipFlowRow {
                        customFileTypeEditor.fileType.fileExtensions.forEachIndexed { i, extension ->
                            FileExtensionChipWithTooltip(
                                extension = extension,
                                deleteExtension = { customFileTypeEditor.deleteExtension(i) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidityIndicatingArea(
    isValid: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .border(1.dp, if (isValid) AppColor.success else AppColor.error, shape = MaterialTheme.shapes.extraSmall)
            .padding(12.dp),
        content = content
    )
}

@Composable
private fun FileExtensionChipWithTooltip(
    extension: String,
    deleteExtension: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            DeletionTooltip(
                onClick = {
                    deleteExtension()
                    tooltipState.dismiss()
                },
                contentDescription = stringResource(com.w2sv.filenavigator.R.string.delete_file_extension)
            )
        },
        state = tooltipState,
        modifier = modifier
    ) {
        FileExtensionChip(extension, onClick = { scope.launch { tooltipState.show(MutatePriority.UserInput) } })
    }
}

@Preview
@Composable
private fun StatelessCustomFileTypeConfigurationDialogPrev() {
    AppTheme {
        StatelessCustomFileTypeConfigurationDialog(
            title = "Create a file type",
            confirmButtonText = "Apply",
            customFileTypeEditor = rememberCustomFileTypeEditor(
                existingFileTypes = persistentSetOf(),
                saveFileType = {},
                initialFileType = CustomFileType("Html", listOf("html", "htm"), Color.Magenta.toArgb(), -1)
            )
                .apply { extensionEditor.update("json") },
            onDismissRequest = {},
            onConfigureColorButtonPress = {},
            excludeFileExtension = { _, _ -> }
        )
    }
}
