package com.w2sv.filenavigator.ui.screen.navigatorsettings.dialogs

import android.content.Context
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.w2sv.common.util.containsSpecialCharacter
import com.w2sv.composed.core.OnChange
import com.w2sv.composed.core.rememberStyledTextResource
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.AnyPresetWrappingFileType
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetWrappingFileType
import com.w2sv.filenavigator.ui.util.InputInvalidityReason
import com.w2sv.filenavigator.ui.util.ProxyTextEditor
import com.w2sv.filenavigator.ui.util.StatefulTextEditor
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.coroutines.flow.emit
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import slimber.log.i

enum class FileTypeNameInvalidityReason(@StringRes override val errorMessageRes: Int) : InputInvalidityReason {
    ContainsSpecialCharacter(R.string.name_must_not_contain_special_characters),
    AlreadyExists(R.string.file_type_already_exists)
}

sealed class FileExtensionInvalidityReason(@StringRes override val errorMessageRes: Int) : InputInvalidityReason {

    val isExcludableExtensionOrNull
        get() = this as? IsExcludableExtension

    data object ContainsSpecialCharacter :
        FileExtensionInvalidityReason(R.string.extension_must_not_contain_special_characters)

    data object AlreadyAmongstAddedExtensions :
        FileExtensionInvalidityReason(R.string.already_amongst_added_extensions)

    sealed class IsExistingFileTypeExtension(@StringRes errorMessageRes: Int) : FileExtensionInvalidityReason(errorMessageRes) {
        abstract val fileExtension: String
        abstract val fileType: FileType

        @Composable
        override fun text(): CharSequence =
            rememberStyledTextResource(errorMessageRes, fileExtension, fileType.label(LocalContext.current))

        companion object {
            // TODO: test
            fun get(fileExtension: String, fileTypes: Collection<FileType>): IsExistingFileTypeExtension? =
                fileTypes
                    .find { fileExtension in it.fileExtensions }
                    ?.let { fileType ->
                        when (fileType) {
                            is CustomFileType, is PresetWrappingFileType.ExtensionConfigurable ->
                                if (fileType.fileExtensions.size == 1) {
                                    IsOnlyFileTypeExtension(
                                        fileExtension,
                                        fileType
                                    )
                                } else {
                                    IsExcludableExtension(fileExtension, fileType)
                                }

                            is PresetWrappingFileType.ExtensionSet -> IsNonExcludableExtension(fileExtension, fileType)
                        }
                    }
        }
    }

    data class IsNonExcludableExtension(override val fileExtension: String, override val fileType: AnyPresetWrappingFileType) :
        IsExistingFileTypeExtension(R.string.is_file_type_extension_and_must_not_be_readded)

    data class IsOnlyFileTypeExtension(override val fileExtension: String, override val fileType: FileType) :
        IsExistingFileTypeExtension(R.string.is_only_file_type_extension_and_must_not_be_readded)

    data class IsExcludableExtension(override val fileExtension: String, override val fileType: FileType) :
        IsExistingFileTypeExtension(R.string.is_other_file_type_extension)
}

@Stable
class CustomFileTypeEditor(
    initialFileType: CustomFileType,
    otherFileTypes: Collection<FileType>,
    private val createFileType: (CustomFileType) -> Unit,
    private val scope: CoroutineScope,
    private val context: Context
) {
    private val otherFileTypeNames by threadUnsafeLazy {
        buildSet { otherFileTypes.forEach { add(it.label(context)) } }
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
                input in otherFileTypeNames -> FileTypeNameInvalidityReason.AlreadyExists
                else -> null
            }
        }
    )

    // ===================
    // Extensions
    // ===================

    fun deleteExtension(@IntRange(from = 0L) index: Int) {
        updateFileType { it.copy(fileExtensions = it.fileExtensions.copy { removeAt(index) }) }
    }

    private var otherFileTypesMutable by mutableStateOf(otherFileTypes)

    fun updateOtherFileTypes(nonMediaFileTypesWithExtensions: Collection<FileType>) {
        this.otherFileTypesMutable = nonMediaFileTypesWithExtensions
    }

    val extensionEditor = StatefulTextEditor(
        processInput = { it.trim().lowercase() },
        findInvalidityReason = { input ->
            when {
                input.containsSpecialCharacter() -> FileExtensionInvalidityReason.ContainsSpecialCharacter
                input in fileType.fileExtensions -> FileExtensionInvalidityReason.AlreadyAmongstAddedExtensions
                else -> FileExtensionInvalidityReason.IsExistingFileTypeExtension.get(input, otherFileTypesMutable)
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

    val canBeCreated by derivedStateOf { nameEditor.isValid && fileType.fileExtensions.isNotEmpty() && fileType != initialFileType }

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
                        otherFileTypes = existingFileTypes,
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
fun rememberCustomFileTypeEditor(
    existingFileTypes: ImmutableSet<FileType>,
    saveFileType: (CustomFileType) -> Unit,
    initialFileType: CustomFileType? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
): CustomFileTypeEditor {
    val editor = rememberSaveable(
        initialFileType,
        saver = CustomFileTypeEditor.saver(existingFileTypes, saveFileType, scope, context)
    ) {
        CustomFileTypeEditor(
            initialFileType = initialFileType ?: CustomFileType.newEmpty(existingFileTypes),
            otherFileTypes = existingFileTypes,
            createFileType = saveFileType,
            scope = scope,
            context = context
        )
    }

    OnChange(existingFileTypes) {
        i { "Updating existingFileTypes to $existingFileTypes" }
        editor.updateOtherFileTypes(it)
    }

    return editor
}
