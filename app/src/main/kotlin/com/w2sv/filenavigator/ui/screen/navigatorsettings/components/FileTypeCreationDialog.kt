package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import android.content.Context
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.util.containsSpecialCharacter
import com.w2sv.common.util.mutate
import com.w2sv.core.domain.R
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.NonMediaFileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.filenavigator.ui.designsystem.DeletionTooltip
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.designsystem.HighlightedDialogButton
import com.w2sv.filenavigator.ui.designsystem.rememberExtendedTooltipState
import com.w2sv.filenavigator.ui.modelext.color
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

private enum class FileTypeNameInvalidityReason(@StringRes override val errorMessageRes: Int) : InputInvalidityReason {
    ContainsSpecialCharacter(com.w2sv.filenavigator.R.string.name_must_not_contain_special_characters),
    AlreadyExists(com.w2sv.filenavigator.R.string.file_type_already_exists)
}

sealed class FileExtensionInvalidityReason(@StringRes override val errorMessageRes: Int) : InputInvalidityReason {
    data object ContainsSpecialCharacter :
        FileExtensionInvalidityReason(com.w2sv.filenavigator.R.string.extension_must_not_contain_special_characters)

    data object AlreadyAmongstAddedExtensions :
        FileExtensionInvalidityReason(com.w2sv.filenavigator.R.string.already_amongst_added_extensions)

    sealed class IsExistingFileExtension<FT : FileType>(@StringRes errorMessageRes: Int) : FileExtensionInvalidityReason(errorMessageRes) {
        protected abstract val fileExtension: String
        protected abstract val fileType: FT

        @Composable
        @ReadOnlyComposable
        override fun text(): String =
            stringResource(errorMessageRes, fileExtension, fileType.label(LocalContext.current))
    }

    data class IsMediaFileTypeExtension(override val fileExtension: String, override val fileType: PresetFileType.Media) :
        IsExistingFileExtension<PresetFileType.Media>(com.w2sv.filenavigator.R.string.is_media_file_type_extension_invalidity_reason) {

        companion object {
            fun get(fileExtension: String): IsMediaFileTypeExtension? =
                PresetFileType.Media.values
                    .firstOrNull { fileExtension in it.fileExtensions }
                    ?.let { mediaFileType -> IsMediaFileTypeExtension(fileExtension, mediaFileType) }
        }
    }

    data class IsOtherNonMediaFileTypeExtension(override val fileExtension: String, override val fileType: NonMediaFileType) :
        IsExistingFileExtension<NonMediaFileType>(com.w2sv.filenavigator.R.string.is_other_non_media_file_type_extension_invalidity_reason) {

        companion object {
            fun get(fileExtension: String, nonMediaFileTypes: Collection<NonMediaFileType>): IsOtherNonMediaFileTypeExtension? =
                nonMediaFileTypes
                    .firstOrNull { fileExtension in it.fileExtensions }
                    ?.let { fileType -> IsOtherNonMediaFileTypeExtension(fileExtension, fileType) }
        }
    }
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

    private val existingNonMediaFileTypes by threadUnsafeLazy {
        existingFileTypes.filterIsInstance<NonMediaFileType>()
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
        processInput = { it.trim() },
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
        updateFileType { it.copy(fileExtensions = it.fileExtensions.mutate { removeAt(index) }) }
    }

    val extensionEditor = StatefulTextEditor(
        processInput = { it.trim().lowercase() },
        findInvalidityReason = { input ->
            when {
                input.containsSpecialCharacter() -> FileExtensionInvalidityReason.ContainsSpecialCharacter
                input in fileType.fileExtensions -> FileExtensionInvalidityReason.AlreadyAmongstAddedExtensions
                else -> FileExtensionInvalidityReason.IsMediaFileTypeExtension.get(input)
                    ?: FileExtensionInvalidityReason.IsOtherNonMediaFileTypeExtension.get(input, existingNonMediaFileTypes)
            }
        }
    )

    fun addExtension() {
        updateFileType { it.copy(fileExtensions = it.fileExtensions + extensionEditor.pop()) }
    }

    // ===================
    // Color
    // ===================

    fun updateColor(color: Color) {
        updateFileType { it.copy(colorInt = color.toArgb()) }
    }

    // ===================
    // Creation
    // ===================

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
            initialFileType = initialFileType ?: CustomFileType.newEmpty(existingFileTypes),
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
    FileTypeConfigurationDialogWithColorPickerDialog(
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
    FileTypeConfigurationDialogWithColorPickerDialog(
        title = stringResource(com.w2sv.filenavigator.R.string.edit_file_type_dialog_title),
        confirmButtonText = stringResource(com.w2sv.filenavigator.R.string.save),
        customFileTypeEditor = rememberCustomFileTypeEditor(fileTypes, createFileType, fileType),
        onDismissRequest = onDismissRequest,
        modifier = modifier
    )
}

@Composable
private fun FileTypeConfigurationDialogWithColorPickerDialog(
    title: String,
    confirmButtonText: String,
    customFileTypeEditor: CustomFileTypeEditor,
    onDismissRequest: () -> Unit,
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
                OutlinedTextField(
                    editor = customFileTypeEditor.nameEditor,
                    placeholderText = stringResource(com.w2sv.filenavigator.R.string.edit_file_type_name_field_placeholder),
                    labelText = stringResource(com.w2sv.filenavigator.R.string.edit_file_type_name_field_label),
                    onApply = customFileTypeEditor::clearFocus
                )
                OutlinedTextField(
                    editor = customFileTypeEditor.extensionEditor,
                    placeholderText = stringResource(com.w2sv.filenavigator.R.string.add_file_extension_field_placeholder),
                    labelText = stringResource(com.w2sv.filenavigator.R.string.file_extension),
                    onApply = customFileTypeEditor::addExtension,
                    applyIconImageVector = Icons.Outlined.Add,
                    showApplyIconOnlyWhenFocused = false,
                    showDisabledApplyButtonWhenEmpty = true
                )
                if (customFileTypeEditor.fileType.fileExtensions.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        customFileTypeEditor.fileType.fileExtensions.forEachIndexed { i, extension ->
                            FileExtensionBadgeWithTooltip(
                                extension = extension,
                                deleteExtension = { customFileTypeEditor.deleteExtension(i) }
                            )
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
                        text = stringResource(com.w2sv.filenavigator.R.string.color),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(customFileTypeEditor.fileType.color)
                        )
                        IconButton(
                            onClick = onConfigureColorButtonPress,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(14.dp, 14.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(28.dp))
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
private fun FileExtensionBadgeWithTooltip(
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
    applyIconImageVector: ImageVector = Icons.Outlined.Check,
    showApplyIconOnlyWhenFocused: Boolean = true,
    showDisabledApplyButtonWhenEmpty: Boolean = false
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
            editor.invalidityReason != null -> {
                {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            }

            editor.isValid && (!showApplyIconOnlyWhenFocused || isFocused) || showDisabledApplyButtonWhenEmpty -> {
                {
                    FilledTonalIconButton(
                        onClick = onApply,
                        modifier = Modifier.padding(end = 4.dp),
                        enabled = editor.isValid,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(contentColor = AppColor.success)
                    ) {
                        Icon(applyIconImageVector, contentDescription = null)
                    }
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
                    text = invalidityReason.text(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        interactionSource = interactionSource
    )
}

// @Preview
// @Composable
// private fun StatelessFileTypeCreationDialogPrev() {
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
// }
