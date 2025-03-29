package com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.core.domain.R
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.NonMediaFileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.filenavigator.ui.designsystem.DeletionTooltip
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.designsystem.HighlightedDialogButton
import com.w2sv.filenavigator.ui.designsystem.OutlinedTextField
import com.w2sv.filenavigator.ui.designsystem.rememberExtendedTooltipState
import com.w2sv.filenavigator.ui.modelext.color
import com.w2sv.filenavigator.ui.modelext.stringResource
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.util.ClearFocusOnFlowEmissionOrKeyboardHidden
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun FileTypeCreationDialog(
    fileTypes: ImmutableSet<FileType>,
    nonMediaFileTypesWithExtensions: ImmutableList<NonMediaFileType.WithExtensions>,
    onDismissRequest: () -> Unit,
    createFileType: (CustomFileType) -> Unit,
    excludeExtensionFromFileType: (String, NonMediaFileType) -> Unit,
    modifier: Modifier = Modifier
) {
    FileTypeConfigurationDialogWithColorPickerDialog(
        title = stringResource(com.w2sv.filenavigator.R.string.create_file_type_dialog_title),
        confirmButtonText = stringResource(com.w2sv.filenavigator.R.string.create),
        customFileTypeEditor = rememberCustomFileTypeEditor(fileTypes, nonMediaFileTypesWithExtensions, createFileType),
        onDismissRequest = onDismissRequest,
        excludeExtensionFromFileType = excludeExtensionFromFileType,
        modifier = modifier
    )
}

@Composable
fun CustomFileTypeConfigurationDialog(
    fileType: CustomFileType,
    fileTypes: ImmutableSet<FileType>,
    nonMediaFileTypesWithExtensions: ImmutableList<NonMediaFileType.WithExtensions>,
    onDismissRequest: () -> Unit,
    createFileType: (CustomFileType) -> Unit,
    excludeExtensionFromFileType: (String, NonMediaFileType) -> Unit,
    modifier: Modifier = Modifier
) {
    FileTypeConfigurationDialogWithColorPickerDialog(
        title = stringResource(com.w2sv.filenavigator.R.string.edit_file_type_dialog_title),
        confirmButtonText = stringResource(com.w2sv.filenavigator.R.string.save),
        customFileTypeEditor = rememberCustomFileTypeEditor(fileTypes, nonMediaFileTypesWithExtensions, createFileType, fileType),
        onDismissRequest = onDismissRequest,
        excludeExtensionFromFileType = excludeExtensionFromFileType,
        modifier = modifier
    )
}

@Composable
private fun FileTypeConfigurationDialogWithColorPickerDialog(
    title: String,
    confirmButtonText: String,
    customFileTypeEditor: CustomFileTypeEditor,
    onDismissRequest: () -> Unit,
    excludeExtensionFromFileType: (String, NonMediaFileType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPickerDialog by rememberSaveable {
        mutableStateOf(false)
    }

    StatelessFileTypeConfigurationDialog(
        title = title,
        confirmButtonText = confirmButtonText,
        customFileTypeEditor = customFileTypeEditor,
        onDismissRequest = onDismissRequest,
        onConfigureColorButtonPress = { showColorPickerDialog = true },
        excludeExtensionFromFileType = excludeExtensionFromFileType,
        modifier = modifier
    )

    if (showColorPickerDialog) {
        ColorPickerDialog(
            initialColor = customFileTypeEditor.fileType.color,
            applyColor = customFileTypeEditor::updateColor,
            onDismissRequest = { showColorPickerDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatelessFileTypeConfigurationDialog(
    title: String,
    confirmButtonText: String,
    customFileTypeEditor: CustomFileTypeEditor,
    onDismissRequest: () -> Unit,
    onConfigureColorButtonPress: () -> Unit,
    excludeExtensionFromFileType: (String, NonMediaFileType) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier
            .pointerInput(Unit) { detectTapGestures { customFileTypeEditor.clearFocus() } },
        icon = { Icon(painterResource(R.drawable.ic_custom_file_type_24), contentDescription = null, modifier = Modifier.size(44.dp)) },
        title = { Text(title) },
        onDismissRequest = onDismissRequest,
        text = {
            ClearFocusOnFlowEmissionOrKeyboardHidden(customFileTypeEditor.clearFocus)

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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
                            actionButton = customFileTypeEditor.extensionEditor.invalidityReason?.isExcludableFileTypeExtensionOrNull?.let { excludableFileTypeExtension ->
                                {
                                    FilledTonalButton(
                                        onClick = {
                                            excludeExtensionFromFileType(
                                                excludableFileTypeExtension.fileExtension,
                                                excludableFileTypeExtension.fileType
                                            )
                                        }
                                    ) { Text("Exclude from ${excludableFileTypeExtension.fileType.stringResource()}") }
                                }
                            }
                        )
                        Spacer(Modifier.height(4.dp))
                        if (customFileTypeEditor.fileType.fileExtensions.isNotEmpty()) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${stringResource(com.w2sv.filenavigator.R.string.color)}:",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .border(Dp.Hairline, MaterialTheme.colorScheme.outline, CircleShape)
                                .background(customFileTypeEditor.fileType.color)
                        )
                        FilledTonalIconButton(
                            onClick = onConfigureColorButtonPress,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                    alpha = 0.7f
                                )
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(8.dp, 8.dp)
                                .border(Dp.Hairline, MaterialTheme.colorScheme.outline, CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            HighlightedDialogButton(
                text = confirmButtonText,
                onClick = {
                    customFileTypeEditor.create()
                    onDismissRequest()
                },
                enabled = customFileTypeEditor.canBeCreated
            )
        },
        dismissButton = {
            DialogButton(text = stringResource(com.w2sv.core.navigator.R.string.cancel), onClick = onDismissRequest)
        }
    )
}

@Composable
private fun ValidityIndicatingArea(
    isValid: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(1.dp, if (isValid) AppColor.success else AppColor.error, shape = MaterialTheme.shapes.extraSmall)
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
private fun FileExtensionChipWithTooltip(
    extension: String,
    deleteExtension: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tooltipState = rememberExtendedTooltipState()

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
        FileExtensionChip(extension, onClick = tooltipState.showTooltip)
    }
}

@Composable
private fun FileExtensionChip(
    extension: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = true,
        onClick = onClick,
        modifier = modifier,
        label = {
            Text(
                text = extension,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        elevation = InputChipDefaults.inputChipElevation(elevation = 4.dp),
        border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.primary)
    )
}

@Preview
@Composable
private fun StatelessFileTypeConfigurationDialogPrev() {
    AppTheme {
        StatelessFileTypeConfigurationDialog(
            title = "Create a file type",
            confirmButtonText = "Apply",
            customFileTypeEditor = rememberCustomFileTypeEditor(
                existingFileTypes = persistentSetOf(),
                nonMediaFileTypesWithExtensions = PresetFileType.NonMedia.ExtensionConfigurable.values.map {
                    PresetFileType.NonMedia.ExtensionConfigured(
                        it,
                        emptySet()
                    )
                }.toImmutableList(),
                createFileType = {},
                initialFileType = CustomFileType("Html", listOf("html", "htm"), Color.Magenta.toArgb(), -1)
            )
                .apply { extensionEditor.update("json") },
            onDismissRequest = {},
            onConfigureColorButtonPress = {},
            excludeExtensionFromFileType = { _, _ -> }
        )
    }
}
