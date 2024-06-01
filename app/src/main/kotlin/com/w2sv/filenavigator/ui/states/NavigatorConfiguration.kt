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
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.sharedviewmodels.MakeSnackbarVisuals
import com.w2sv.kotlinutils.extensions.toNonZeroInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted

@Stable
class NavigatorConfiguration(
    scope: CoroutineScope,
    navigatorConfigDataSource: NavigatorConfigDataSource,
    private val emitMakeSnackbarVisuals: (MakeSnackbarVisuals) -> Unit,
    onStateSynced: () -> Unit,
) {
    // =======================
    // FileType Enablement
    // =======================

    val fileTypeEnablementMap = ReversibleStateMap(
        appliedStateMap = navigatorConfigDataSource.fileTypeEnablementMap,
        makeMap = { it.toMutableStateMap() },
        syncState = {
            navigatorConfigDataSource.fileTypeEnablementMap.save(it)
        },
        onStateSynced = { syncedMap ->
            enabledFileTypes.updateFrom(FileType.values.filter { syncedMap.getValue(it) })
            disabledFileTypes.updateFrom(FileType.values.filter { !syncedMap.getValue(it) })
        },
        onStateReset = { resetMap ->
            enabledFileTypes.updateFrom(FileType.values.filter { resetMap.getValue(it) })
            disabledFileTypes.updateFrom(FileType.values.filter { !resetMap.getValue(it) })
        },
        appliedStateMapBasedStateAlignmentAssuranceScope = scope
    )

    val enabledFileTypes: SnapshotStateList<FileType> by lazy {
        FileType.values
            .filter { navigatorConfigDataSource.fileTypeEnablementMap.getValue(it).value }
            .toMutableStateList()
    }
    val disabledFileTypes by lazy {
        FileType.values
            .filter { !navigatorConfigDataSource.fileTypeEnablementMap.getValue(it).value }
            .toMutableStateList()
    }

    fun onFileTypeCheckedChange(
        fileType: FileType,
        checkedNew: Boolean
    ) {
        if (!checkedNew && enabledFileTypes.size <= 1) {
            emitMakeSnackbarVisuals {
                AppSnackbarVisuals(
                    message = it.getString(
                        R.string.leave_at_least_one_file_type_enabled
                    ),
                    kind = SnackbarKind.Error
                )
            }
        } else {
            fileTypeEnablementMap[fileType] = checkedNew
            if (checkedNew) {
                enabledFileTypes.add(fileType)
                disabledFileTypes.remove(fileType)
            } else {
                disabledFileTypes.add(fileType)
                enabledFileTypes.remove(fileType)
            }
        }
    }

    // =======================
    // MediaSourceType Enablement
    // =======================

    val mediaFileSourceEnablementMap = ReversibleStateMap(
        appliedStateMap = navigatorConfigDataSource.mediaFileSourceEnablementMap,
        makeMap = { it.toMutableStateMap() },
        syncState = {
            navigatorConfigDataSource.mediaFileSourceEnablementMap.save(it)
        },
        appliedStateMapBasedStateAlignmentAssuranceScope = scope
    )

    fun onMediaFileSourceCheckedChange(source: SourceType, checkedNew: Boolean) {
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
                    SourceTypes.count { source ->
                        mediaFileSourceEnablementMap.getOrDefault(
                            source,
                            true
                        )
                    }
                }
                .toMutableMap()
        )
    }

    // ==========
    // More
    // ==========

    val disableOnLowBattery = ReversibleStateFlow(
        scope = scope,
        dataStoreFlow = navigatorConfigDataSource.disableOnLowBattery,
        started = SharingStarted.Eagerly,
        appliedStateBasedStateAlignmentPostInit = true
    )

    val unconfirmedStates = ReversibleStatesComposition(
        reversibleStates = listOf(
            fileTypeEnablementMap,
            mediaFileSourceEnablementMap,
            disableOnLowBattery
        ),
        scope = scope,
        onStateSynced = {
            fileTypeToNCheckedSources.sync()
            onStateSynced()
        },
        onStateReset = {
            fileTypeToNCheckedSources.reset()
        }
    )
}

private fun <T> SnapshotStateList<T>.updateFrom(other: List<T>) {  // TODO: export to composed
    other.forEachIndexed { index, venue ->
        try {
            if (venue != get(index)) {
                this[index] = venue
            }
        } catch (e: IndexOutOfBoundsException) {
            add(venue)
        }
    }
    if (size > other.size) {
        removeRange(other.size, size)
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