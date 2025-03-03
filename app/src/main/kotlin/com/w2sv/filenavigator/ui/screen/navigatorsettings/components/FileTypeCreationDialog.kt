package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipScope
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.common.util.containsSpecialCharacter
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.util.ClearFocusOnFlowEmissionOrKeyboardHidden
import com.w2sv.kotlinutils.coroutines.flow.emit
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.text.trim
import com.w2sv.core.domain.R

interface InputInvalidityReason {
    val errorMessage: String
}

private enum class FileTypeNameInvalidityReason(override val errorMessage: String) : InputInvalidityReason {
    ContainsSpecialCharacter("Name must not contain special characters"),
    AlreadyExists("File type already exists")
}

private enum class FileExtensionInvalidityReason(override val errorMessage: String) : InputInvalidityReason {
    ContainsSpecialCharacter("Extension must not contain special characters"),
    AlreadyAmongstAddedExtensions("Already amongst added extensions")
}

@Stable
class TextEditor<T : InputInvalidityReason> private constructor(
    private val initialText: String,
    private val mutableState: MutableState<String>,
    private val processInput: (String) -> String,
    private val findInvalidityReason: (String) -> T?
) :
    State<String> by mutableState {

    constructor(initialText: String = "", processInput: (String) -> String = { it }, findInvalidityReason: (String) -> T? = { null })
        : this(
        initialText = initialText,
        mutableState = mutableStateOf(initialText),
        processInput = processInput,
        findInvalidityReason = findInvalidityReason
    )

    fun update(input: String) {
        mutableState.value = processInput(input)
    }

    val invalidityReason by derivedStateOf { findInvalidityReason(value) }
    val isValid by derivedStateOf { invalidityReason == null && value.isNotBlank() }

    fun pop(): String = value.also { mutableState.value = initialText }
}

@Stable
private class CustomFileTypeEditor(
    private val scope: CoroutineScope,
    private val existingFileTypes: Collection<FileType>,
    private val context: Context
) {
    private val existingFileTypeNames by threadUnsafeLazy {
        buildSet { existingFileTypes.forEach { add(it.label(context)) } }
    }

    val nameEditor = TextEditor(
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

    val extensions = mutableStateListOf<String>()

    fun deleteExtension(index: Int) {
        extensions.removeAt(index)
    }

    val extensionEditor = TextEditor(
        processInput = { it.trim().lowercase() },
        findInvalidityReason = { input ->
            when {
                input.containsSpecialCharacter() -> FileExtensionInvalidityReason.ContainsSpecialCharacter
                input in extensions -> FileExtensionInvalidityReason.AlreadyAmongstAddedExtensions
                else -> null
            }
        }
    )

    fun addExtension() {
        extensions.add(extensionEditor.pop())
    }

    val canBeCreated by derivedStateOf { nameEditor.isValid && extensions.isNotEmpty() }

    // ===================
    // Focus
    // ===================

    val clearFocus get() = _clearFocus.asSharedFlow()
    private val _clearFocus = MutableSharedFlow<Unit>()

    fun clearFocus() {
        _clearFocus.emit(Unit, scope)
    }

    fun toCustomFileType(): CustomFileType =
        CustomFileType(
            name = nameEditor.value,
            fileExtensions = extensions,
            ordinal = existingFileTypes.map { it.ordinal }.max() + 1  // TODO
        )
}

@Composable
fun FileTypeCreationDialog(
    fileTypes: ImmutableSet<FileType>,
    onDismissRequest: () -> Unit,
    onCreateFileType: (CustomFileType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val customFileTypeEditor = remember(fileTypes) { CustomFileTypeEditor(scope, fileTypes, context) }  // TODO: rememberSavable

    StatelessFileTypeCreationDialog(customFileTypeEditor, onDismissRequest, onCreateFileType, modifier)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatelessFileTypeCreationDialog(
    customFileTypeEditor: CustomFileTypeEditor,
    onDismissRequest: () -> Unit,
    createFileType: (CustomFileType) -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier.pointerInput(Unit) { detectTapGestures { customFileTypeEditor.clearFocus() } },
        icon = { Icon(painterResource(R.drawable.ic_custom_file_type_24), contentDescription = null, modifier = Modifier.size(42.dp)) },
        title = { Text("Create a file type") },
        onDismissRequest = onDismissRequest,
        text = {
            ClearFocusOnFlowEmissionOrKeyboardHidden(customFileTypeEditor.clearFocus)

            Column {
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    editor = customFileTypeEditor.nameEditor,
                    placeholderText = "Enter name",
                    labelText = "Name",
                    onApply = customFileTypeEditor::clearFocus
                )
                OutlinedTextField(
                    editor = customFileTypeEditor.extensionEditor,
                    placeholderText = "Enter file extension",
                    onApply = customFileTypeEditor::addExtension,
                    modifier = Modifier
                        .width(192.dp)
                        .padding(vertical = 16.dp),
                    applyIconImageVector = Icons.Outlined.Add
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    customFileTypeEditor.extensions.forEachIndexed { i, extension ->
                        FileExtensionBadgeWithTooltip(extension = extension, deleteExtension = { customFileTypeEditor.deleteExtension(i) })
                    }
                }
            }
        },
        confirmButton = {
            DialogButton(
                text = "Create",
                onClick = {
                    createFileType(customFileTypeEditor.toCustomFileType())
                    onDismissRequest()
                },
                enabled = customFileTypeEditor.canBeCreated
            )
        },
    )
}

@Composable
private fun FileExtensionBadgeWithTooltip(
    extension: String,
    deleteExtension: () -> Unit,
    modifier: Modifier = Modifier,
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val tooltipState = remember { TooltipState() }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            FileExtensionDeletionTooltip(
                onClick = {
                    deleteExtension()
                    tooltipState.dismiss()
                }
            )
        },
        state = tooltipState,
        modifier = modifier
    ) {
        FileExtensionBadge(extension, modifier = Modifier.clickable { scope.launch { tooltipState.show() } })
    }
}

@SuppressLint("ComposeUnstableReceiver")
@Composable
private fun TooltipScope.FileExtensionDeletionTooltip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PlainTooltip(caretSize = TooltipDefaults.caretSize, tonalElevation = 4.dp, shadowElevation = 4.dp, modifier = modifier) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete file extension",
            )
        }
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
        value = editor.value,
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
                    text = invalidityReason.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        interactionSource = interactionSource
    )
}

@Preview
@Composable
private fun StatelessFileTypeCreationDialogPrev() {
    AppTheme {
        val context = LocalContext.current
        StatelessFileTypeCreationDialog(
            customFileTypeEditor = CustomFileTypeEditor(rememberCoroutineScope(), emptyList(), context)
                .apply {
                    extensions.addAll(listOf("jpg", "png", "jpgasdf"))
                    extensionEditor.update("dot")
                },
            onDismissRequest = {},
            createFileType = {}
        )
    }
}
