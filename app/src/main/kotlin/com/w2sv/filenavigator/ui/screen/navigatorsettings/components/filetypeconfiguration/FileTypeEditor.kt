package com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration

import android.content.Context
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
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
import androidx.compose.ui.res.stringResource
import com.w2sv.common.util.containsSpecialCharacter
import com.w2sv.common.util.mutate
import com.w2sv.composed.OnChange
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.FileExtensionsHolder
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.NonMediaFileType
import com.w2sv.domain.model.PresetFileType
import com.w2sv.filenavigator.ui.util.InputInvalidityReason
import com.w2sv.filenavigator.ui.util.ProxyTextEditor
import com.w2sv.filenavigator.ui.util.StatefulTextEditor
import com.w2sv.kotlinutils.coroutines.flow.emit
import com.w2sv.kotlinutils.threadUnsafeLazy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import slimber.log.i
import kotlin.collections.forEach
import kotlin.getValue

enum class FileTypeNameInvalidityReason(@StringRes override val errorMessageRes: Int) : InputInvalidityReason {
    ContainsSpecialCharacter(com.w2sv.filenavigator.R.string.name_must_not_contain_special_characters),
    AlreadyExists(com.w2sv.filenavigator.R.string.file_type_already_exists)
}

sealed class FileExtensionInvalidityReason(@StringRes override val errorMessageRes: Int) : InputInvalidityReason {

    val isExcludableFileTypeExtensionOrNull
        get() = this as? IsExcludableFileTypeExtension

    data object ContainsSpecialCharacter :
        FileExtensionInvalidityReason(com.w2sv.filenavigator.R.string.extension_must_not_contain_special_characters)

    data object AlreadyAmongstAddedExtensions :
        FileExtensionInvalidityReason(com.w2sv.filenavigator.R.string.already_amongst_added_extensions)

    sealed class IsExistingFileExtension<FT : FileType>(@StringRes errorMessageRes: Int) : FileExtensionInvalidityReason(errorMessageRes) {
        abstract val fileExtension: String
        abstract val fileType: FT

        @Composable
        @ReadOnlyComposable
        override fun text(): String =
            stringResource(errorMessageRes, fileExtension, fileType.label(LocalContext.current))

        companion object {
            // TODO: test
            fun get(fileExtension: String, nonMediaFileTypes: Collection<NonMediaFileType.WithExtensions>): IsExistingFileExtension<*>? =
                PresetFileType.Media.values
                    .findMatching(fileExtension)
                    ?.let { mediaFileType -> IsNonExcludableFileTypeExtension(fileExtension, mediaFileType) }
                    ?: nonMediaFileTypes
                        .findMatching(fileExtension)
                        ?.let { fileType ->
                            if (fileType.fileExtensions.size == 1) {
                                IsNonExcludableFileTypeExtension(fileExtension, fileType)
                            } else {
                                IsExcludableFileTypeExtension(fileExtension, fileType)
                            }
                        }
        }
    }

    data class IsNonExcludableFileTypeExtension(override val fileExtension: String, override val fileType: FileType) :
        IsExistingFileExtension<FileType>(com.w2sv.filenavigator.R.string.is_media_file_type_extension_invalidity_reason)

    data class IsExcludableFileTypeExtension(
        override val fileExtension: String,
        override val fileType: NonMediaFileType.WithExtensions
    ) :
        IsExistingFileExtension<NonMediaFileType.WithExtensions>(
            com.w2sv.filenavigator.R.string.is_other_non_media_file_type_extension_invalidity_reason
        )
}

private fun <T : FileExtensionsHolder> Collection<T>.findMatching(fileExtension: String): T?? =
    find { fileExtension in it.fileExtensions }

@Stable
class CustomFileTypeEditor(
    initialFileType: CustomFileType,
    private val existingFileTypes: Collection<FileType>,
    initialNonMediaFileTypesWithExtensions: Collection<NonMediaFileType.WithExtensions>,
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

    private var nonMediaFileTypesWithExtensions by mutableStateOf(initialNonMediaFileTypesWithExtensions)

    fun updateNonMediaFileTypesWithExtensions(nonMediaFileTypesWithExtensions: Collection<NonMediaFileType.WithExtensions>) {
        this.nonMediaFileTypesWithExtensions = nonMediaFileTypesWithExtensions
    }

    val extensionEditor = StatefulTextEditor(
        processInput = { it.trim().lowercase() },
        findInvalidityReason = { input ->
            when {
                input.containsSpecialCharacter() -> FileExtensionInvalidityReason.ContainsSpecialCharacter
                input in fileType.fileExtensions -> FileExtensionInvalidityReason.AlreadyAmongstAddedExtensions
                else -> FileExtensionInvalidityReason.IsExistingFileExtension.get(input, nonMediaFileTypesWithExtensions)
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
            nonMediaFileTypesWithExtensions: ImmutableList<NonMediaFileType.WithExtensions>,
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
                        initialNonMediaFileTypesWithExtensions = nonMediaFileTypesWithExtensions,
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
    nonMediaFileTypesWithExtensions: ImmutableList<NonMediaFileType.WithExtensions>,
    createFileType: (CustomFileType) -> Unit,
    initialFileType: CustomFileType? = null,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
): CustomFileTypeEditor {
    val editor = rememberSaveable(
        initialFileType,
        existingFileTypes,
        saver = CustomFileTypeEditor.saver(existingFileTypes, nonMediaFileTypesWithExtensions, createFileType, scope, context)
    ) {
        CustomFileTypeEditor(
            initialFileType = initialFileType ?: CustomFileType.newEmpty(existingFileTypes),
            existingFileTypes = existingFileTypes,
            initialNonMediaFileTypesWithExtensions = nonMediaFileTypesWithExtensions,
            createFileType = createFileType,
            scope = scope,
            context = context
        )
    }

    OnChange(nonMediaFileTypesWithExtensions) {
        i { "Updating nonMediaFileTypesWithExtensions to $nonMediaFileTypesWithExtensions" }
        editor.updateNonMediaFileTypesWithExtensions(it)
    }

    return editor
}
