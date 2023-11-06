package com.w2sv.filenavigator.ui.states

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.common.utils.goToManageExternalStorageSettings
import com.w2sv.common.utils.manageExternalStoragePermissionRequired
import com.w2sv.data.model.FileType
import com.w2sv.data.storage.preferences.repositories.FileTypeRepository
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.SnackbarAction
import com.w2sv.filenavigator.ui.components.SnackbarKind
import com.w2sv.filenavigator.ui.utils.extensions.allFalseAfterEnteringValue
import com.w2sv.filenavigator.ui.utils.extensions.getMutableStateList
import com.w2sv.filenavigator.ui.utils.extensions.getSynchronousMutableStateMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FileTypesState(
    val statusMap: UnconfirmedStateMap<DataStoreEntry.EnumValued<FileType.Status>, FileType.Status>,
    val mediaFileSourceEnabledMap: UnconfirmedStateMap<DataStoreEntry.UniType<Boolean>, Boolean>,
    onStateSynced: () -> Unit,
    private val scope: CoroutineScope,
    statusMapChanged: MutableSharedFlow<Unit>
) : UnconfirmedStatesComposition(
    unconfirmedStates = listOf(
        statusMap,
        mediaFileSourceEnabledMap,
    ),
    coroutineScope = scope,
    onStateSynced = onStateSynced
) {
    constructor(
        scope: CoroutineScope,
        fileTypeRepository: FileTypeRepository,
        onStateSynced: () -> Unit,
        statusMapChanged: MutableSharedFlow<Unit> = MutableSharedFlow(),
    ) : this(
        scope = scope,
        statusMapChanged = statusMapChanged,
        statusMap = UnconfirmedStateMap(
            coroutineScope = scope,
            appliedFlowMap = fileTypeRepository.fileTypeStatus,
            getDefaultValue = {
                fileTypeRepository.fileTypeStatus.getValue(it).getValueSynchronously()
            },
            makeMap = { it.getSynchronousMutableStateMap() },
            syncState = {
                fileTypeRepository.saveEnumValuedMap(it)
            },
            onStateSynced = {
                statusMapChanged.emit(Unit)
            }
        ),
        mediaFileSourceEnabledMap = UnconfirmedStateMap(
            coroutineScope = scope,
            appliedFlowMap = fileTypeRepository.mediaFileSourceEnabled,
            getDefaultValue = {
                fileTypeRepository.mediaFileSourceEnabled.getValue(it).getValueSynchronously()
            },
            makeMap = { it.getSynchronousMutableStateMap() },
            syncState = { fileTypeRepository.saveMap(it) }
        ),
        onStateSynced = onStateSynced
    )

    val sortedFileTypes: SnapshotStateList<FileType> =
        FileType.getValues()
            .getMutableStateList()
            .apply {
                sortByIsEnabledAndOriginalOrder(statusMap)
            }

    private fun getFirstDisabledFileType(): FileType? =
        sortedFileTypes.getFirstDisabledFileType { !statusMap.appliedIsEnabled(it) }

    val firstDisabledFileType get() = _firstDisabledFileType.asStateFlow()
    private val _firstDisabledFileType = MutableStateFlow(getFirstDisabledFileType())

    init {
        scope.collectFromFlow(statusMapChanged) {
            sortedFileTypes.sortByIsEnabledAndOriginalOrder(statusMap)
            _firstDisabledFileType.value = getFirstDisabledFileType()
        }
    }

    fun onFileTypeCheckedChange(
        fileType: FileType,
        checkedNew: Boolean,
        showSnackbar: (AppSnackbarVisuals) -> Unit,
        context: Context
    ) {
        when (val result = getFileTypeCheckedChangeResult(
            statusMap.getValue(fileType.statusDSE),
            checkedNew
        )) {
            is FileTypeCheckedChangeResult.ToggleStatus -> statusMap.toggle(fileType.statusDSE)
            is FileTypeCheckedChangeResult.ShowSnackbar -> showSnackbar(
                result.getAppSnackbarVisuals(
                    context
                )
            )
        }
    }

    private fun getFileTypeCheckedChangeResult(
        status: FileType.Status,
        checkedNew: Boolean
    ): FileTypeCheckedChangeResult =
        when (status) {
            FileType.Status.Enabled, FileType.Status.Disabled -> {
                if (statusMap.allowToggle(checkedNew)) {
                    FileTypeCheckedChangeResult.ToggleStatus
                } else {
                    FileTypeCheckedChangeResult.ShowSnackbar.LeaveAtLeastOneFileTypeEnabled
                }
            }

            else -> FileTypeCheckedChangeResult.ShowSnackbar.AllFilesAccessRequired
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

        data object AllFilesAccessRequired : ShowSnackbar {
            override fun getAppSnackbarVisuals(context: Context): AppSnackbarVisuals =
                AppSnackbarVisuals(
                    message = context.getString(R.string.non_media_files_require_all_files_access),
                    kind = SnackbarKind.Error,
                    action = SnackbarAction(
                        label = context.getString(R.string.grant),
                        callback = {
                            if (manageExternalStoragePermissionRequired()) {
                                goToManageExternalStorageSettings(context)
                            }
                        }
                    )
                )
        }
    }
}

private fun MutableList<FileType>.sortByIsEnabledAndOriginalOrder(fileTypeStatuses: Map<DataStoreEntry.EnumValued<FileType.Status>, FileType.Status>) {
    sortWith(
        compareByDescending<FileType> {
            fileTypeStatuses.getValue(
                it.statusDSE
            )
                .isEnabled
        }
            .thenBy(FileType.getValues()::indexOf)
    )
}

/**
 * Assumes value corresponding to [key] to be one of [FileType.Status.Enabled] or [FileType.Status.Disabled].
 */
private fun <K> MutableMap<K, FileType.Status>.toggle(key: K) {
    put(
        key,
        if (getValue(key) == FileType.Status.Disabled) FileType.Status.Enabled else FileType.Status.Disabled
    )
}

private fun List<FileType>.getFirstDisabledFileType(isDisabled: (FileType) -> Boolean): FileType? =
    windowed(2)
        .firstOrNull { !isDisabled(it[0]) && isDisabled(it[1]) }
        ?.let { it[1] }

private fun UnconfirmedStateMap<DataStoreEntry.EnumValued<FileType.Status>, FileType.Status>.appliedIsEnabled(
    fileType: FileType
): Boolean {
    val isEnabled = getValue(fileType.statusDSE).isEnabled
    val statesDissimilar = dissimilarKeys.contains(fileType.statusDSE)

    return (isEnabled && !statesDissimilar) || (!isEnabled && statesDissimilar)
}

private fun UnconfirmedStateMap<DataStoreEntry.EnumValued<FileType.Status>, FileType.Status>.allowToggle(
    checkedNew: Boolean
): Boolean =
    !values
        .map { it.isEnabled }
        .allFalseAfterEnteringValue(
            checkedNew
        )