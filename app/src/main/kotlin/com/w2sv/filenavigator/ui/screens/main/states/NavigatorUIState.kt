package com.w2sv.filenavigator.ui.screens.main.states

import android.content.Context
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.data.model.FileType
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.data.storage.repositories.PreferencesRepository
import com.w2sv.filenavigator.ui.model.sortByIsEnabledAndOriginalOrder
import com.w2sv.filenavigator.ui.screens.main.components.filetypeselection.defaultmovedestination.DefaultMoveDestinationConfiguration
import com.w2sv.filenavigator.ui.utils.getMutableStateList
import com.w2sv.filenavigator.ui.utils.getSynchronousMutableStateMap
import com.w2sv.navigator.FileNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class NavigatorUIState(
    private val scope: CoroutineScope,
    private val fileTypeRepository: FileTypeRepository,
    private val preferencesRepository: PreferencesRepository,
    context: Context
) {
    val isRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileNavigator>())

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

    fun getDefaultMoveDestinationConfiguration(fileSource: FileType.Source): DefaultMoveDestinationConfiguration =
        DefaultMoveDestinationConfiguration(
            moveDestination = UnconfirmedStateFlow(
                coroutineScope = scope,
                appliedFlow = fileTypeRepository.getFileSourceDefaultDestinationFlow(fileSource),
                syncState = { fileTypeRepository.saveFileSourceDefaultDestination(fileSource, it) }
            ),
            isLocked = UnconfirmedStateFlow(
                coroutineScope = scope,
                appliedFlow = fileTypeRepository.getFileSourceDefaultDestinationIsLockedFlow(
                    fileSource
                ),
                syncState = {
                    fileTypeRepository.saveFileSourceDefaultDestinationIsLocked(
                        fileSource,
                        it
                    )
                }
            ),
            scope = scope
        )
}