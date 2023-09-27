package com.w2sv.filenavigator.ui.states

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.common.utils.goToManageExternalStorageSettings
import com.w2sv.common.utils.manageExternalStoragePermissionRequired
import com.w2sv.data.model.FileType
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.data.storage.repositories.PreferencesRepository
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppSnackbarVisuals
import com.w2sv.filenavigator.ui.components.SnackbarAction
import com.w2sv.filenavigator.ui.components.SnackbarKind
import com.w2sv.filenavigator.ui.model.sortByIsEnabledAndOriginalOrder
import com.w2sv.filenavigator.ui.model.toggle
import com.w2sv.filenavigator.ui.utils.extensions.allFalseAfterEnteringValue
import com.w2sv.filenavigator.ui.utils.extensions.getMutableStateList
import com.w2sv.filenavigator.ui.utils.extensions.getSynchronousMutableStateMap
import com.w2sv.navigator.FileNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class NavigatorState(
    private val scope: CoroutineScope,
    private val fileTypeRepository: FileTypeRepository,
    private val preferencesRepository: PreferencesRepository,
    context: Context
) {
    val isRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileNavigator>())

    val startDateTime = preferencesRepository.navigatorStartDateTime.stateIn(
        scope,
        SharingStarted.WhileSubscribed(),
        null
    )

    val disableOnLowBattery = preferencesRepository.disableNavigatorOnLowBattery.stateIn(
        scope,
        SharingStarted.WhileSubscribed(),
    )

    fun saveDisableOnLowBattery(value: Boolean) {
        scope.launch {
            preferencesRepository.disableNavigatorOnLowBattery.save(value)
        }
    }

    val sortedFileTypes = FileType.values
        .getMutableStateList()

    fun List<FileType>.isFirstAppliedDisabled(i: Int): Boolean =
        i >= 1 && !fileTypeStatusMap.appliedIsEnabled(get(i)) && fileTypeStatusMap.appliedIsEnabled(
            get(i - 1)
        )

    private val sortFileTypes = MutableSharedFlow<Unit>()

    val fileTypeStatusMap =
        UnconfirmedStateMap(
            coroutineScope = scope,
            appliedFlowMap = fileTypeRepository.fileTypeStatus,
            makeSynchronousMutableMap = { it.getSynchronousMutableStateMap() },
            syncState = {
                fileTypeRepository.saveEnumValuedMap(it)
                sortFileTypes.emit(Unit)
            }
        )

    fun onFileTypeCheckedChange(
        fileType: FileType,
        checkedNew: Boolean,
        showSnackbar: (AppSnackbarVisuals) -> Unit,
        context: Context
    ) {
        when (val result = getFileTypeCheckedChangeResult(
            fileTypeStatusMap.getValue(fileType.status),
            checkedNew
        )) {
            is FileTypeCheckedChangeResult.ToggleStatus -> fileTypeStatusMap.toggle(fileType.status)
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
                if (fileTypeStatusMap.allowToggle(checkedNew)) {
                    FileTypeCheckedChangeResult.ToggleStatus
                } else {
                    FileTypeCheckedChangeResult.ShowSnackbar.LeaveAtLeastOneFileTypeEnabled
                }
            }

            else -> FileTypeCheckedChangeResult.ShowSnackbar.AllFilesAccessRequired
        }


    init {
        sortedFileTypes.sortByIsEnabledAndOriginalOrder(fileTypeStatusMap)

        scope.launch {
            sortFileTypes.collect {
                sortedFileTypes.sortByIsEnabledAndOriginalOrder(fileTypeStatusMap)
            }
        }
    }

    val mediaFileSourceEnabledMap = UnconfirmedStateMap(
        coroutineScope = scope,
        appliedFlowMap = fileTypeRepository.mediaFileSourceEnabled,
        makeSynchronousMutableMap = { it.getSynchronousMutableStateMap() },
        syncState = { fileTypeRepository.saveMap(it) }
    )

    val configuration = UnconfirmedStatesComposition(
        unconfirmedStates = listOf(
            fileTypeStatusMap,
            mediaFileSourceEnabledMap
        ),
        coroutineScope = scope
    )

    internal fun setFileTypeStatus(fileTypes: Iterable<FileType>, newStatus: FileType.Status) {
        fileTypes.forEach {
            fileTypeStatusMap[it.status] = newStatus
        }
        scope.launch {
            fileTypeStatusMap.sync()
        }
    }

    val defaultMoveDestinationState = DefaultMoveDestinationState(fileTypeRepository, scope)
}

class DefaultMoveDestinationState(
    private val fileTypeRepository: FileTypeRepository,
    private val scope: CoroutineScope
) {
    val stateFlowMap =
        fileTypeRepository.defaultDestinationMap.mapValues { (_, v) ->
            v.stateIn(
                scope,
                SharingStarted.Eagerly,
                null
            )
        }

    // ==================
    // Configuration
    // ==================

    val selectionSource = MutableStateFlow<FileType.Source?>(null)

    fun onDestinationSelected(treeUri: Uri, context: Context) {
        DocumentFile.fromTreeUri(context, treeUri)?.let { documentFile ->
            saveDestination(selectionSource.value!!, documentFile.uri)
        }
        selectionSource.value = null
    }

    fun saveDestination(source: FileType.Source, destination: Uri?) {
        scope.launch {
            fileTypeRepository.saveDefaultDestination(source, destination)
        }
    }
}

fun getDefaultMoveDestinationPath(uri: Uri, context: Context): String? =
    DocumentFile.fromSingleUri(context, uri)?.getSimplePath(context)

sealed interface FileTypeCheckedChangeResult {
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

fun UnconfirmedStateMap<FileType.Status.StoreEntry, FileType.Status>.appliedIsEnabled(fileType: FileType): Boolean {
    val isEnabled = getValue(fileType.status).isEnabled
    val statesDissimilar = dissimilarKeys.contains(fileType.status)

    return (isEnabled && !statesDissimilar) || (!isEnabled && statesDissimilar)
}

fun UnconfirmedStateMap<FileType.Status.StoreEntry, FileType.Status>.allowToggle(checkedNew: Boolean): Boolean =
    !values
        .map { it.isEnabled }
        .allFalseAfterEnteringValue(
            checkedNew
        )