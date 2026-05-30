package com.w2sv.filenavigator.ui.screen.navigatorsettings

import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.reversiblestate.ReversibleState
import com.w2sv.reversiblestate.ReversibleStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ReversibleNavigatorConfig(reversibleStateFlow: ReversibleStateFlow<NavigatorConfig>) :
    ReversibleState by reversibleStateFlow,
    MutableStateFlow<NavigatorConfig> by reversibleStateFlow {

    constructor(
        scope: CoroutineScope,
        navigatorConfigDataSource: NavigatorConfigDataSource,
        onStateSynced: suspend () -> Unit
    ) : this(
        reversibleStateFlow = ReversibleStateFlow(
            scope = scope,
            appliedState = navigatorConfigDataSource.config.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = NavigatorConfig.default
            ),
            commitState = { state ->
                navigatorConfigDataSource.update { state }
                onStateSynced()
            }
        )
    )

    fun toggleFileTypeEnablement(fileType: FileType) {
        update { it.toggleFileTypeEnablement(fileType) }
    }

    fun editFileType(current: FileType, edited: FileType) {
        update { it.editFileType(current) { edited } }
    }

    fun createCustomFileType(type: FileType.Custom) {
        update { it.addCustomFileType(type) }
    }

    fun deleteCustomFileType(type: FileType.Custom) {
        update { it.deleteCustomFileType(type) }
    }

    /**
     * @param fileType Must be either a custom file type or a preset file type with configurable extensions.
     * TODO: test
     */
    fun excludeFileExtension(fileType: FileType, extension: String) {
        require(fileType !is FileType.FixedPreset) { "$fileType doesn't support extension exclusion" }

        when (fileType) {
            is FileType.Custom -> update {
                it.editFileType(fileType) {
                    fileType.withFileExtensions(it.fileExtensions - extension)
                }
            }

            is FileType.ConfigurablePreset -> update {
                it.editFileType(fileType) {
                    fileType.withExcludedExtensions(fileType.excludedExtensions + extension)
                }
            }
        }
    }
}
