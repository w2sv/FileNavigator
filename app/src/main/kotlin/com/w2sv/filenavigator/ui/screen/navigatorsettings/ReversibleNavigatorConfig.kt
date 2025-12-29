package com.w2sv.filenavigator.ui.screen.navigatorsettings

import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetWrappingFileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.copy
import com.w2sv.reversiblestate.ReversibleState
import com.w2sv.reversiblestate.ReversibleStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
            appliedStateFlow = navigatorConfigDataSource.navigatorConfig.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed()
            ),
            syncState = {
                navigatorConfigDataSource.navigatorConfig.save(it)
                onStateSynced()
            }
        )
    )

    fun toggleFileTypeEnablement(fileType: FileType) {
        update { it.toggleFileTypeEnablement(fileType) }
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
}
