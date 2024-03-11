package com.w2sv.filenavigator.ui.states

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateFlow
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateMap
import com.w2sv.androidutils.ui.reversible_state.ReversibleStatesComposition
import com.w2sv.common.utils.ReversibleValue
import com.w2sv.common.utils.increment
import com.w2sv.composed.extensions.toMutableStateMap
import com.w2sv.domain.model.FileType
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.sharedviewmodels.MakeSnackbarVisuals
import com.w2sv.kotlinutils.extensions.toInt
import com.w2sv.kotlinutils.extensions.toNonZeroInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow

@Stable
class NavigatorConfiguration(
    scope: CoroutineScope,
    navigatorRepository: NavigatorRepository,
    private val emitMakeSnackbarVisuals: (MakeSnackbarVisuals) -> Unit,
    onStateSynced: () -> Unit,
) {
    private val statusMapChanged = MutableSharedFlow<Unit>()

    val statusMap = ReversibleStateMap.fromPersistedFlowMapWithSynchronousInitial(
        persistedFlowMap = navigatorRepository.getFileTypeEnablementMap(),
        scope = scope,
        makeMap = { it.toMutableStateMap() },
        syncState = {
            navigatorRepository.saveFileTypeEnablementMap(it)
            statusMapChanged.emit(Unit)
        }
    )

    val mediaFileSourceEnabledMap = ReversibleStateMap.fromPersistedFlowMapWithSynchronousInitial(
        persistedFlowMap = navigatorRepository.getMediaFileSourceEnablementMap(),
        scope = scope,
        makeMap = { it.toMutableStateMap() },
        syncState = {
            navigatorRepository.saveMediaFileSourceEnablementMap(it)
        }
    )

    val disableOnLowBattery = ReversibleStateFlow(
        scope,
        navigatorRepository.disableOnLowBattery,
        SharingStarted.Eagerly
    )

    val unconfirmedStates = ReversibleStatesComposition(
        reversibleStates = listOf(
            statusMap,
            mediaFileSourceEnabledMap,
            disableOnLowBattery
        ),
        coroutineScope = scope,
        onStateSynced = {
            nCheckedFileTypes.sync()
            fileTypeToNCheckedSources.sync()
            onStateSynced()
        },
        onStateReset = {
            nCheckedFileTypes.reset()
            fileTypeToNCheckedSources.reset()
        }
    )

    val sortedFileTypes: SnapshotStateList<FileType> =
        FileType.values
            .toMutableStateList()
            .apply {
                sortByIsEnabledAndOriginalOrder(statusMap)
            }

    private fun getFirstDisabledFileType(): FileType? =
        sortedFileTypes.getFirstDisabled { !statusMap.persistedStateFlowMap.getValue(it).value }

    val firstDisabledFileType get() = _firstDisabledFileType.asStateFlow()
    private val _firstDisabledFileType = MutableStateFlow(getFirstDisabledFileType())

    init {
        scope.collectFromFlow(statusMapChanged) {
            sortedFileTypes.sortByIsEnabledAndOriginalOrder(statusMap)
            _firstDisabledFileType.value = getFirstDisabledFileType()
        }
    }

    fun onMediaFileSourceCheckedChange(source: FileType.Source, checkedNew: Boolean) {
        if (!checkedNew && fileTypeToNCheckedSources.value.getValue(source.fileType) <= 1) {
            emitMakeSnackbarVisuals {
                AppSnackbarVisuals(
                    message = it.getString(R.string.leave_at_least_one_file_source_selected_or_disable_the_entire_file_type),
                    kind = SnackbarKind.Error
                )
            }
        } else {
            mediaFileSourceEnabledMap[source] = checkedNew
            fileTypeToNCheckedSources.value.increment(source.fileType, checkedNew.toNonZeroInt())
        }
    }

    private val fileTypeToNCheckedSources: ReversibleValue<MutableMap<FileType, Int>> =
        ReversibleValue(
            FileType.Media.values
                .associateWith { fileType ->
                    var nCheckedSources = 0
                    fileType.sources.forEach { source ->
                        nCheckedSources += mediaFileSourceEnabledMap.getOrDefault(
                            source,
                            true
                        )
                            .toInt()
                    }
                    nCheckedSources
                }
                .toMutableMap()
        )

    fun onFileTypeCheckedChange(
        fileType: FileType,
        checkedNew: Boolean
    ) {
        if (!checkedNew && nCheckedFileTypes.value <= 1) {
            emitMakeSnackbarVisuals {
                AppSnackbarVisuals(
                    message = it.getString(
                        R.string.leave_at_least_one_file_type_enabled
                    ),
                    kind = SnackbarKind.Error
                )
            }
        } else {
            statusMap[fileType] = checkedNew
            nCheckedFileTypes.value += checkedNew.toNonZeroInt()
        }
    }

    private val nCheckedFileTypes = ReversibleValue(statusMap.values.count { it })
}

private fun MutableList<FileType>.sortByIsEnabledAndOriginalOrder(fileTypeStatuses: Map<FileType, Boolean>) {
    sortWith(
        compareByDescending<FileType> {
            fileTypeStatuses.getValue(
                it
            )
        }
            .thenBy(FileType.values::indexOf)
    )
}

private fun List<FileType>.getFirstDisabled(isDisabled: (FileType) -> Boolean): FileType? =
    windowed(2)
        .firstOrNull { !isDisabled(it[0]) && isDisabled(it[1]) }
        ?.let { it[1] }