package com.w2sv.filenavigator.ui.screen.navigatorsettings

import androidx.compose.runtime.Stable
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetWrappingFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.util.MakeSnackbarVisuals
import com.w2sv.kotlinutils.copy
import com.w2sv.reversiblestate.ReversibleState
import com.w2sv.reversiblestate.ReversibleStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update

@Stable
class ReversibleNavigatorConfig(
    reversibleStateFlow: ReversibleStateFlow<NavigatorConfig>,
    private val makeSnackbarVisuals: (MakeSnackbarVisuals) -> Unit
) : ReversibleState by reversibleStateFlow,
    MutableStateFlow<NavigatorConfig> by reversibleStateFlow {

    constructor(
        scope: CoroutineScope,
        navigatorConfigDataSource: NavigatorConfigDataSource,
        makeSnackbarVisuals: (MakeSnackbarVisuals) -> Unit,
        onStateSynced: () -> Unit
    ) : this(
        reversibleStateFlow = ReversibleStateFlow(
            scope = scope,
            appliedStateFlow = navigatorConfigDataSource.navigatorConfig.stateIn(
                scope = scope,
                started = SharingStarted.Companion.WhileSubscribed()
            ),
            syncState = {
                navigatorConfigDataSource.navigatorConfig.save(it)
                onStateSynced()
            }
        ),
        makeSnackbarVisuals = makeSnackbarVisuals
    )

    fun toggleFileTypeEnablement(fileType: FileType) {
        update { it.toggleFileTypeEnablement(fileType) }
    }

    fun onFileSourceCheckedChange(
        fileType: FileType,
        sourceType: SourceType,
        checkedNew: Boolean
    ) {
        updateOrEmitSnackbar(
            checkedNew = checkedNew,
            checkedCount = value.enabledSourceTypesCount(fileType),
            update = {
                update { config ->
                    config.updateSourceConfig(
                        fileType = fileType,
                        sourceType = sourceType
                    ) {
                        it.copy(enabled = checkedNew)
                    }
                }
            },
            makeSnackbarVisuals = {
                AppSnackbarVisuals(
                    message = it.getString(R.string.leave_at_least_one_file_source_selected_or_disable_the_entire_file_type),
                    kind = SnackbarKind.Error
                )
            }
        )
    }

    fun <T : FileType> editFileType(current: T, edited: T) {
        update { it.editFileType(current) { edited } }
    }

    fun createCustomFileType(type: CustomFileType) {
        update { it.addCustomFileType(type) }
    }

    fun deleteCustomFileType(type: CustomFileType) {
        update { it.deleteCustomFileType(type) }
    }

    /**
     * @param fileType Must be either [CustomFileType] or [com.w2sv.domain.model.filetype.PresetWrappingFileType.ExtensionConfigurable]
     * TODO: test
     */
    fun excludeFileExtension(fileType: FileType, extension: String) {
        when (fileType) {
            is CustomFileType -> update {
                it.editFileType(fileType) {
                    fileType.copy(fileExtensions = it.fileExtensions.copy { remove(extension) })
                }
            }

            is PresetWrappingFileType.ExtensionConfigurable -> update {
                it.editFileType(fileType) { it.copy(excludedExtensions = it.excludedExtensions + extension) }
            }

            is PresetWrappingFileType.ExtensionSet -> error("$fileType of type PresetWrappingFileType.ExtensionSet should not be passed")
        }
    }

    private inline fun updateOrEmitSnackbar(
        checkedNew: Boolean,
        checkedCount: Int,
        update: () -> Unit,
        crossinline makeSnackbarVisuals: MakeSnackbarVisuals
    ) {
        if (!checkedNew && checkedCount <= 1) {
            makeSnackbarVisuals { makeSnackbarVisuals(it) }
        } else {
            update()
        }
    }
}
