package com.w2sv.filenavigator.ui.states

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.composed.extensions.toMutableStateMap
import com.w2sv.domain.model.FileType
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.utils.allFalseAfterEnteringValue
import com.w2sv.filenavigator.ui.utils.toggle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow

@Stable
class NavigatorConfiguration(
    val statusMap: UnconfirmedStateMap<FileType, Boolean>,
    val mediaFileSourceEnabledMap: UnconfirmedStateMap<FileType.Source, Boolean>,
    val disableOnLowBattery: UnconfirmedStateFlow<Boolean>,
    onStateSynced: () -> Unit,
    private val scope: CoroutineScope,
    statusMapChanged: MutableSharedFlow<Unit>
) : UnconfirmedStatesComposition(
    unconfirmedStates = listOf(
        statusMap,
        mediaFileSourceEnabledMap,
        disableOnLowBattery
    ),
    coroutineScope = scope,
    onStateSynced = onStateSynced
) {
    constructor(
        scope: CoroutineScope,
        navigatorRepository: NavigatorRepository,
        onStateSynced: () -> Unit,
        statusMapChanged: MutableSharedFlow<Unit> = MutableSharedFlow(),
    ) : this(
        statusMapChanged = statusMapChanged,
        statusMap = UnconfirmedStateMap.fromPersistedFlowMapWithSynchronousInitial(
            persistedFlowMap = navigatorRepository.getFileTypeEnablementMap(),
            scope = scope,
            makeMap = { it.toMutableStateMap() },
            syncState = {
                navigatorRepository.saveFileTypeEnablementMap(it)
            },
            onStateSynced = {
                statusMapChanged.emit(Unit)
            }
        ),
        mediaFileSourceEnabledMap = UnconfirmedStateMap.fromPersistedFlowMapWithSynchronousInitial(
            persistedFlowMap = navigatorRepository.getMediaFileSourceEnablementMap(),
            scope = scope,
            makeMap = { it.toMutableStateMap() },
            syncState = { navigatorRepository.saveMediaFileSourceEnablementMap(it) }
        ),
        disableOnLowBattery = UnconfirmedStateFlow(
            scope,
            navigatorRepository.disableOnLowBattery,
            SharingStarted.Eagerly
        ),
        onStateSynced = onStateSynced,
        scope = scope,
    )

    val sortedFileTypes: SnapshotStateList<FileType> =
        FileType.getValues()
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

    fun onFileTypeCheckedChangeInput(
        fileType: FileType,
        checkedNew: Boolean,
        showSnackbar: (AppSnackbarVisuals) -> Unit,
        context: Context
    ) {
        when (val result = getFileTypeCheckedChangeResult(checkedNew)) {
            is FileTypeCheckedChangeResult.ToggleStatus -> statusMap.toggle(fileType)
            is FileTypeCheckedChangeResult.ShowSnackbar -> showSnackbar(
                result.getAppSnackbarVisuals(
                    context
                )
            )
        }
    }

    private fun getFileTypeCheckedChangeResult(
        checkedNew: Boolean
    ): FileTypeCheckedChangeResult =
        if (statusMap.values.allFalseAfterEnteringValue(checkedNew)) {
            FileTypeCheckedChangeResult.ShowSnackbar.LeaveAtLeastOneFileTypeEnabled
        } else {
            FileTypeCheckedChangeResult.ToggleStatus
        }
}

private sealed interface FileTypeCheckedChangeResult {
    data object ToggleStatus : FileTypeCheckedChangeResult

    sealed interface ShowSnackbar : FileTypeCheckedChangeResult {
        fun getAppSnackbarVisuals(context: Context): AppSnackbarVisuals

        data object LeaveAtLeastOneFileTypeEnabled : ShowSnackbar {
            override fun getAppSnackbarVisuals(context: Context): AppSnackbarVisuals =
                AppSnackbarVisuals(
                    message = context.getString(
                        R.string.leave_at_least_one_file_type_enabled
                    ),
                    kind = SnackbarKind.Error
                )
        }
    }
}

private fun MutableList<FileType>.sortByIsEnabledAndOriginalOrder(fileTypeStatuses: Map<FileType, Boolean>) {
    sortWith(
        compareByDescending<FileType> {
            fileTypeStatuses.getValue(
                it
            )
        }
            .thenBy(FileType.getValues()::indexOf)
    )
}

private fun List<FileType>.getFirstDisabled(isDisabled: (FileType) -> Boolean): FileType? =
    windowed(2)
        .firstOrNull { !isDisabled(it[0]) && isDisabled(it[1]) }
        ?.let { it[1] }