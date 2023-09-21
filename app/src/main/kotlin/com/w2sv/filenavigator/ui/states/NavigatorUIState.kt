package com.w2sv.filenavigator.ui.states

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.data.model.FileType
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.data.storage.repositories.PreferencesRepository
import com.w2sv.filenavigator.ui.model.sortByIsEnabledAndOriginalOrder
import com.w2sv.filenavigator.ui.utils.extensions.getMutableStateList
import com.w2sv.filenavigator.ui.utils.extensions.getSynchronousMutableStateMap
import com.w2sv.navigator.FileNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NavigatorUIState(
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

    fun getDefaultMoveDestinationState(source: FileType.Source): DefaultMoveDestinationState =
        DefaultMoveDestinationState(
            destination = fileTypeRepository.getDefaultDestinationFlow(source).stateIn(
                scope,
                SharingStarted.Eagerly,
                null
            ),
            saveDestination = {
                scope.launch {
                    fileTypeRepository.saveDefaultDestination(source, it)
                }
            }
        )
}

class DefaultMoveDestinationState(
    val destination: StateFlow<Uri?>,
    val saveDestination: (Uri?) -> Unit
) {
    fun onDestinationSelected(treeUri: Uri?, context: Context) {
        if (treeUri != null) {
            DocumentFile.fromTreeUri(context, treeUri)?.let { documentFile ->
                saveDestination(documentFile.uri)
            }
        }
    }

    val isDestinationSet = destination.mapState { it != null }
}

fun UnconfirmedStateMap<FileType.Status.StoreEntry, FileType.Status>.appliedIsEnabled(fileType: FileType): Boolean {
    val isEnabled = getValue(fileType.status).isEnabled
    val statesDissimilar = dissimilarKeys.contains(fileType.status)

    return (isEnabled && !statesDissimilar) || (!isEnabled && statesDissimilar)
}