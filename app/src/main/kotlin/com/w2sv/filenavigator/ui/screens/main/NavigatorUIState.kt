package com.w2sv.filenavigator.ui.screens.main

import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.data.model.FileType
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.filenavigator.ui.model.sortByIsEnabledAndOriginalOrder
import com.w2sv.filenavigator.ui.utils.getMutableStateList
import com.w2sv.filenavigator.ui.utils.getSynchronousMutableStateMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NavigatorUIState(
    private val scope: CoroutineScope,
    private val fileTypeRepository: FileTypeRepository
) {
    val fileTypeStatusMap =
        UnconfirmedStateMap(
            coroutineScope = scope,
            appliedFlowMap = fileTypeRepository.fileTypeStatus,
            makeSynchronousMutableMap = { it.getSynchronousMutableStateMap() },
            syncState = { fileTypeRepository.saveEnumValuedMap(it) }
        )

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
        syncFileTypeStatuses()
    }

    val sortedFileTypes = FileType.values
        .getMutableStateList()
        .apply {
            sortByIsEnabledAndOriginalOrder(fileTypeStatusMap)
        }

    fun syncFileTypeStatuses(): Job =
        scope.launch {
            fileTypeStatusMap.sync()
            sortedFileTypes.sortByIsEnabledAndOriginalOrder(fileTypeStatusMap)
        }
}