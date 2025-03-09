package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import android.content.Context
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.common.util.containsSpecialCharacter
import com.w2sv.core.domain.R
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.ui.designsystem.DeletionTooltip
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.designsystem.rememberExtendedTooltipState
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.util.ClearFocusOnFlowEmissionOrKeyboardHidden
import com.w2sv.filenavigator.ui.util.InputInvalidityReason
import com.w2sv.filenavigator.ui.util.ProxyTextEditor
import com.w2sv.filenavigator.ui.util.StatefulTextEditor
import com.w2sv.filenavigator.ui.util.TextEditor
import com.w2sv.kotlinutils.coroutines.flow.emit
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.text.trim

private enum class FileTypeNameInvalidityReason(@StringRes override val errorMessageId: Int) : InputInvalidityReason {
    ContainsSpecialCharacter(com.w2sv.filenavigator.R.string.name_must_not_contain_special_characters),
    AlreadyExists(com.w2sv.filenavigator.R.string.file_type_already_exists)
}

private enum class FileExtensionInvalidityReason(@StringRes override val errorMessageId: Int) : InputInvalidityReason {
    ContainsSpecialCharacter(com.w2sv.filenavigator.R.string.extension_must_not_contain_special_characters),
    AlreadyAmongstAddedExtensions(com.w2sv.filenavigator.R.string.already_amongst_added_extensions)
}

@Stable
private class CustomFileTypeEditor(
    initialFileType: CustomFileType,
    private val existingFileTypes: Collection<FileType>,
    private val createFileType: (CustomFileType) -> Unit,
    private val scope: CoroutineScope,
    private val context: Context
) {
    private val existingFileTypeNames by threadUnsafeLazy {
        buildSet { existingFileTypes.forEach { add(it.label(context)) } }
    }

    var fileType by mutableStateOf(initialFileType)
        private set

    private fun updateFileType(update: (CustomFileType) -> CustomFileType) {
        fileType = update(fileType)
    }

    // ===================
    // Name
    // ===================

    val nameEditor = ProxyTextEditor(
        getValue = { fileType.name },
        setValue = { value -> updateFileType { it.copy(name = value) } },
        processInput = { it.trim().replaceFirstChar(Char::titlecase) },
        findInvalidityReason = { input ->
            when {
                input.containsSpecialCharacter() -> FileTypeNameInvalidityReason.ContainsSpecialCharacter
                input in existingFileTypeNames -> FileTypeNameInvalidityReason.AlreadyExists
                else -> null
            }
        }
    )

    // ===================
    // Extensions
    // ===================

    fun deleteExtension(@IntRange(from = 0L) index: Int) {
        updateFileType { it.copy(fileExtensions = it.fileExtensions.toMutableList().apply { removeAt(index) }) }
    }

    val extensionEditor = StatefulTextEditor(
        processInput = { it.trim().lowercase() },
        findInvalidityReason = { input ->
            when {
                input.containsSpecialCharacter() -> FileExtensionInvalidityReason.ContainsSpecialCharacter
                input in initialFileType.fileExtensions -> FileExtensionInvalidityReason.AlreadyAmongstAddedExtensions
                else -> null
            }
        }
    )

    fun addExtension() {
        updateFileType { it.copy(fileExtensions = it.fileExtensions + extensionEditor.pop()) }
    }

    fun create() {
        createFileType(fileType)
    }

    val canBeCreated by derivedStateOf { nameEditor.isValid && fileType.fileExtensions.isNotEmpty() }

    // ===================
    // Focus
    // ===================

    val clearFocus get() = _clearFocus.asSharedFlow()
    private val _clearFocus = MutableSharedFlow<Unit>()

    fun clearFocus() {
        _clearFocus.emit(Unit, scope)
    }

    companion object {
        fun saver(
            existingFileTypes: Collection<FileType>,
            createFileType: (CustomFileType) -> Unit,
            scope: CoroutineScope,
            context: Context
        ): Saver<CustomFileTypeEditor, Pair<CustomFileType, String>> =
            object : Saver<CustomFileTypeEditor, Pair<CustomFileType, String>> {
                override fun SaverScope.save(value: CustomFileTypeEditor): Pair<CustomFileType, String> =
                    value.fileType to value.extensionEditor.getValue()

                override fun restore(value: Pair<CustomFileType, String>): CustomFileTypeEditor =
                    CustomFileTypeEditor(
                        initialFileType = value.first,
                        existingFileTypes = existingFileTypes,
                        createFileType = createFileType,
                        scope = scope,
                        context = context
                    ).apply {
                        extensionEditor.update(value.second)
                    }
            }
    }
}

@Composable
private fun rememberCustomFileTypeEditor(
    existingFileTypes: ImmutableSet<FileType>,
    createFileType: (CustomFileType) -> Unit,
    initialFileType: CustomFileType? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
): CustomFileTypeEditor {
    return rememberSaveable(
        initialFileType,
        existingFileTypes,
        saver = CustomFileTypeEditor.saver(existingFileTypes, createFileType, scope, context)
    ) {
        CustomFileTypeEditor(
            initialFileType = initialFileType ?: CustomFileType(
                name = "",
                fileExtensions = emptyList(),
                ordinal = CustomFileType.ordinal(existingFileTypes)
            ),
            existingFileTypes = existingFileTypes,
            createFileType = createFileType,
            scope = scope,
            context = context
        )
    }
}

@Composable
fun FileTypeCreationDialog(
    fileTypes: ImmutableSet<FileType>,
    onDismissRequest: () -> Unit,
    createFileType: (CustomFileType) -> Unit,
    modifier: Modifier = Modifier
) {
    StatelessFileTypeConfigurationDialog(
        title = stringResource(com.w2sv.filenavigator.R.string.create_file_type_dialog_title),
        confirmButtonText = stringResource(com.w2sv.filenavigator.R.string.create),
        customFileTypeEditor = rememberCustomFileTypeEditor(fileTypes, createFileType),
        onDismissRequest = onDismissRequest,
        modifier = modifier
    )
}

@Composable
fun CustomFileTypeConfigurationDialog(
    fileType: CustomFileType,
    fileTypes: ImmutableSet<FileType>,
    onDismissRequest: () -> Unit,
    createFileType: (CustomFileType) -> Unit,
    modifier: Modifier = Modifier
) {
    StatelessFileTypeConfigurationDialog(
        title = stringResource(com.w2sv.filenavigator.R.string.edit_file_type_dialog_title),
        confirmButtonText = stringResource(com.w2sv.filenavigator.R.string.save),
        customFileTypeEditor = rememberCustomFileTypeEditor(fileTypes, createFileType, fileType),
        onDismissRequest = onDismissRequest,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatelessFileTypeConfigurationDialog(
    title: String,
    confirmButtonText: String,
    customFileTypeEditor: CustomFileTypeEditor,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier
            .pointerInput(Unit) { detectTapGestures { customFileTypeEditor.clearFocus() } },
        icon = { Icon(painterResource(R.drawable.ic_custom_file_type_24), contentDescription = null, modifier = Modifier.size(42.dp)) },
        title = { Text(title) },
        onDismissRequest = onDismissRequest,
        text = {
            ClearFocusOnFlowEmissionOrKeyboardHidden(customFileTypeEditor.clearFocus)

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    editor = customFileTypeEditor.nameEditor,
                    placeholderText = stringResource(com.w2sv.filenavigator.R.string.edit_file_type_name_field_placeholder),
                    labelText = stringResource(com.w2sv.filenavigator.R.string.edit_file_type_name_field_label),
                    onApply = customFileTypeEditor::clearFocus
                )
                OutlinedTextField(
                    editor = customFileTypeEditor.extensionEditor,
                    placeholderText = stringResource(com.w2sv.filenavigator.R.string.add_file_extension_field_placeholder),
                    onApply = customFileTypeEditor::addExtension,
                    modifier = Modifier
                        .width(192.dp)
                        .padding(vertical = 16.dp),
                    applyIconImageVector = Icons.Outlined.Add
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    customFileTypeEditor.fileType.fileExtensions.forEachIndexed { i, extension ->
                        FileExtensionBadgeWithTooltip(extension = extension, deleteExtension = { customFileTypeEditor.deleteExtension(i) })
                    }
                }
            }
        },
        confirmButton = {
            DialogButton(
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
private fun FileExtensionBadgeWithTooltip(
    extension: String,
    deleteExtension: () -> Unit,
    modifier: Modifier = Modifier,
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
        FileExtensionBadge(extension, modifier = Modifier.clickable(onClick = tooltipState.showTooltip))
    }
}

@Composable
private fun FileExtensionBadge(extension: String, modifier: Modifier = Modifier) {
    Badge(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier
    ) {
        Text(
            text = extension,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun OutlinedTextField(
    editor: TextEditor<*>,
    placeholderText: String,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    labelText: String? = null,
    applyIconImageVector: ImageVector = Icons.Outlined.Check
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    OutlinedTextField(
        value = editor.getValue(),
        onValueChange = { editor.update(it) },
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = { Text(placeholderText, maxLines = 1) },
        label = labelText?.let { { Text(labelText, maxLines = 1) } },
        singleLine = true,
        modifier = modifier,
        trailingIcon = when {
            editor.isValid && isFocused -> {
                {
                    FilledTonalIconButton(onClick = onApply, modifier = Modifier.padding(end = 4.dp)) {
                        Icon(applyIconImageVector, contentDescription = null, tint = AppColor.success)
                    }
                }
            }

            editor.invalidityReason != null -> {
                {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                null
            }
        },
        isError = editor.invalidityReason != null,
        supportingText = editor.invalidityReason?.let { invalidityReason ->
            {
                Text(
                    text = stringResource(invalidityReason.errorMessageId),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        interactionSource = interactionSource
    )
}

//@Preview
//@Composable
//private fun StatelessFileTypeCreationDialogPrev() {
//    AppTheme {
//        val context = LocalContext.current
//        StatelessFileTypeCreationDialog(
//            customFileTypeEditor = CustomFileTypeEditor(rememberCoroutineScope(), emptyList(), context)
//                .apply {
//                    extensions.addAll(listOf("jpg", "png", "jpgasdf"))
//                    extensionEditor.update("dot")
//                },
//            onDismissRequest = {},
//            createFileType = {}
//        )
//    }
//}
