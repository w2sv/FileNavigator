package com.w2sv.filenavigator.ui.states

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateFlow
import com.w2sv.androidutils.ui.reversible_state.ReversibleStateMap
import com.w2sv.androidutils.ui.reversible_state.ReversibleStatesComposition
import com.w2sv.common.utils.increment
import com.w2sv.composed.extensions.toMutableStateMap
import com.w2sv.domain.model.FileType
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.sharedviewmodels.MakeSnackbarVisuals
import com.w2sv.kotlinutils.extensions.toNonZeroInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
class NavigatorConfiguration(
    scope: CoroutineScope,
    navigatorRepository: NavigatorRepository,
    private val emitMakeSnackbarVisuals: (MakeSnackbarVisuals) -> Unit,
    onStateSynced: () -> Unit,
) {
    val sortedFileTypes: SnapshotStateList<FileType> =
        FileType.values
            .toMutableStateList()
            .apply {
                sortByIsEnabledAndOriginalOrder(
                    isEnabled = {
                        navigatorRepository.fileTypeEnablementMap.getValue(
                            it
                        )
                            .value
                    }
                )
            }

    val firstDisabledFileType: StateFlow<FileType?> get() = _firstDisabledFileType.asStateFlow()
    private val _firstDisabledFileType = MutableStateFlow(
        sortedFileTypes.firstDisabled {
            navigatorRepository.fileTypeEnablementMap.getValue(it).value
        }
    )

    val fileEnablementMap = ReversibleStateMap(
        appliedStateMap = navigatorRepository.fileTypeEnablementMap,
        makeMap = { it.toMutableStateMap() },
        syncState = {
            navigatorRepository.fileTypeEnablementMap.save(it)
        },
        onStateSynced = { map ->
            sortedFileTypes.sortByIsEnabledAndOriginalOrder(isEnabled = { map.getValue(it) })
            _firstDisabledFileType.value = sortedFileTypes.firstDisabled {
                map.getValue(it)
            }
        },
        appliedStateMapBasedStateAlignmentAssuranceScope = scope
    )

    val mediaFileSourceEnablementMap = ReversibleStateMap(
        appliedStateMap = navigatorRepository.mediaFileSourceEnablementMap,
        makeMap = { it.toMutableStateMap() },
        syncState = {
            navigatorRepository.mediaFileSourceEnablementMap.save(it)
        },
        appliedStateMapBasedStateAlignmentAssuranceScope = scope
    )

    val disableOnLowBattery = ReversibleStateFlow(
        scope = scope,
        dataStoreFlow = navigatorRepository.disableOnLowBattery,
        started = SharingStarted.Eagerly,
        appliedStateBasedStateAlignmentPostInit = true
    )

    val unconfirmedStates = ReversibleStatesComposition(
        reversibleStates = listOf(
            fileEnablementMap,
            mediaFileSourceEnablementMap,
            disableOnLowBattery
        ),
        scope = scope,
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

    fun onMediaFileSourceCheckedChange(source: FileType.Source, checkedNew: Boolean) {
        if (!checkedNew && fileTypeToNCheckedSources.value.getValue(source.fileType) <= 1) {
            emitMakeSnackbarVisuals {
                AppSnackbarVisuals(
                    message = it.getString(R.string.leave_at_least_one_file_source_selected_or_disable_the_entire_file_type),
                    kind = SnackbarKind.Error
                )
            }
        } else {
            mediaFileSourceEnablementMap[source] = checkedNew
            fileTypeToNCheckedSources.value.increment(source.fileType, checkedNew.toNonZeroInt())
        }
    }

    private val fileTypeToNCheckedSources: ReversibleValue<MutableMap<FileType, Int>> by lazy {
        ReversibleValue(
            FileType.Media.values
                .associateWith { fileType ->
                    fileType.sources.count { source ->
                        mediaFileSourceEnablementMap.getOrDefault(
                            source,
                            true
                        )
                    }
                }
                .toMutableMap()
        )
    }

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
            fileEnablementMap[fileType] = checkedNew
            nCheckedFileTypes.value += checkedNew.toNonZeroInt()
        }
    }

    private val nCheckedFileTypes by lazy {
        ReversibleValue(fileEnablementMap.values.count { it })
    }
}

private data class ReversibleValue<T>(var value: T) {
    var previous: T = value
        private set

    fun sync() {
        previous = value
    }

    fun reset() {
        value = previous
    }
}

private fun MutableList<FileType>.sortByIsEnabledAndOriginalOrder(isEnabled: (FileType) -> Boolean) {
    sortWith(
        compareByDescending(isEnabled)
            .thenBy(FileType.values::indexOf)
    )
}

private inline fun List<FileType>.firstDisabled(isEnabled: (FileType) -> Boolean): FileType? =
    firstOrNull { !isEnabled(it) }
