package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.androidutils.ui.unconfirmed_state.getUnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.getUnconfirmedStateMap
import com.w2sv.androidutils.ui.unconfirmed_state.getUnconfirmedStatesComposition
import com.w2sv.data.model.FileType
import com.w2sv.data.model.StorageAccessStatus
import com.w2sv.data.model.Theme
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.data.storage.repositories.PreferencesRepository
import com.w2sv.filenavigator.ui.model.sortByIsEnabledAndOriginalOrder
import com.w2sv.filenavigator.ui.utils.getMutableStateList
import com.w2sv.filenavigator.ui.utils.getMutableStateMap
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val fileTypeRepository: FileTypeRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val isNavigatorRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileNavigator>())

    val disableListenerOnLowBattery = preferencesRepository.disableListenerOnLowBattery.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        true
    )

    fun saveDisableListenerOnLowBattery(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveDisableListenerOnLowBattery(value)
        }
    }

    fun saveShowedManageExternalStorageRational(): Job =
        viewModelScope.launch {
            preferencesRepository.saveShowedManageExternalStorageRational()
        }

    val showedPostNotificationsPermissionsRational by preferencesRepository::showedPostNotificationsPermissionsRational

    fun saveShowedPostNotificationsPermissionsRational() {
        viewModelScope.launch { preferencesRepository.saveShowedPostNotificationsPermissionsRational() }
    }

    // ==============
    // StorageAccessStatus
    // ==============

    private val storageAccessStatus: StateFlow<StorageAccessStatus> get() = _storageAccessStatus
    private val _storageAccessStatus = MutableStateFlow(StorageAccessStatus.NoAccess)

    val anyStorageAccessGranted: StateFlow<Boolean> =
        storageAccessStatus.mapState { it != StorageAccessStatus.NoAccess }

    val showManageExternalStorageDialog = combine(
        preferencesRepository.showedManageExternalStorageRational,
        anyStorageAccessGranted,
    ) { f1, f2 ->
        println("showedManageExternalStorageRational: $f1 anyStorageAccessGranted: $f2")
        !f1 && !f2
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun updateStorageAccessStatus(context: Context) {
        _storageAccessStatus.value = StorageAccessStatus.get(context)
            .also { status ->
                val previousStatus =
                    preferencesRepository.previousStorageAccessStatus.getValueSynchronously()

                if (status != previousStatus) {
                    i { "New manageExternalStoragePermissionGranted = $status diverting from previous = $previousStatus" }

                    when (status) {
                        StorageAccessStatus.NoAccess -> setFileTypeStatuses(
                            FileType.values,
                            FileType.Status.DisabledDueToNoFileAccess
                        )

                        StorageAccessStatus.MediaFilesOnly -> {
                            setFileTypeStatuses(
                                FileType.NonMedia.all,
                                FileType.Status.DisabledDueToMediaAccessOnly
                            )

                            if (previousStatus == StorageAccessStatus.NoAccess) {
                                setFileTypeStatuses(FileType.Media.all, FileType.Status.Enabled)
                            }
                        }

                        StorageAccessStatus.AllFiles -> setFileTypeStatuses(
                            FileType.values,
                            FileType.Status.Enabled
                        )
                    }

                    viewModelScope.launch {
                        preferencesRepository.savePreviousStorageAccessStatus(status)
                    }
                }
            }
    }

    private fun setFileTypeStatuses(fileTypes: Iterable<FileType>, newStatus: FileType.Status) {
        fileTypes.forEach {
            unconfirmedFileTypeStatus[it.status] = newStatus
        }
        launchUnconfirmedFileTypeStatusSync()
    }

    // ==============
    // Navigator Configuration
    // ==============

    val sortedFileTypes = FileType.values
        .getMutableStateList()

    val unconfirmedFileTypeStatus =
        getUnconfirmedStateMap(
            appliedFlowMap = fileTypeRepository.fileTypeStatus,
            makeSynchronousMutableMap = { it.getSynchronousMap().getMutableStateMap() },
            syncState = { fileTypeRepository.saveEnumValuedMap(it) }
        )
            .also {
                sortedFileTypes.sortByIsEnabledAndOriginalOrder(it)
            }

    fun launchUnconfirmedFileTypeStatusSync(): Job =
        viewModelScope.launch {
            unconfirmedFileTypeStatus.sync()
            sortedFileTypes.sortByIsEnabledAndOriginalOrder(unconfirmedFileTypeStatus)
//            fileTypeStatusHasChanged.emit(true)
        }

//    val fileTypeStatusHasChanged = MutableStateFlow(false)

    val unconfirmedFileSourceEnablement by lazy {
        getUnconfirmedStateMap(
            appliedFlowMap = fileTypeRepository.mediaFileSourceEnabled,
            makeSynchronousMutableMap = { it.getSynchronousMap().getMutableStateMap() },
            syncState = { fileTypeRepository.saveMap(it) }
        )
    }

    val unconfirmedNavigatorConfiguration by lazy {
        getUnconfirmedStatesComposition(
            listOf(
                unconfirmedFileTypeStatus,
                unconfirmedFileSourceEnablement
            )
        )
    }

    fun setUnconfirmedDefaultMoveDestinationStates(fileSource: FileType.Source) {
        unconfirmedDefaultMoveDestination = getUnconfirmedStateFlow(
            appliedFlow = fileTypeRepository.getFileSourceDefaultDestinationFlow(fileSource),
            syncState = { fileTypeRepository.saveFileSourceDefaultDestination(fileSource, it) }
        )
        unconfirmedDefaultMoveDestinationIsLocked = getUnconfirmedStateFlow(
            appliedFlow = fileTypeRepository.getFileSourceDefaultDestinationIsLockedFlow(fileSource),
            syncState = {
                fileTypeRepository.saveFileSourceDefaultDestinationIsLocked(
                    fileSource,
                    it
                )
            }
        )

        unconfirmedDefaultMoveDestinationConfiguration = getUnconfirmedStatesComposition(
            listOf(
                unconfirmedDefaultMoveDestination!!,
                unconfirmedDefaultMoveDestinationIsLocked!!
            )
        )
    }

    fun unsetUnconfirmedDefaultMoveDestinationStates() {
        unconfirmedDefaultMoveDestination = null
        unconfirmedDefaultMoveDestinationIsLocked = null
        unconfirmedDefaultMoveDestinationConfiguration = null
    }

    val defaultMoveDestinationIsSet = fileTypeRepository.getUriFlowMap(
        FileType.values.map {
            it.sources
        }
            .flatten()
            .map { it.defaultDestination }
    )
        .getSynchronousMap()
        .mapValues { it.value != null }
        .getMutableStateMap()

    var unconfirmedDefaultMoveDestination: UnconfirmedStateFlow<Uri?>? = null
    var unconfirmedDefaultMoveDestinationIsLocked: UnconfirmedStateFlow<Boolean>? = null
    var unconfirmedDefaultMoveDestinationConfiguration: UnconfirmedStatesComposition? = null

    fun onDefaultMoveDestinationSelected(treeUri: Uri?, context: Context) {
        if (treeUri != null) {
            DocumentFile.fromTreeUri(context, treeUri)?.let { documentFile ->
                unconfirmedDefaultMoveDestination!!.value = documentFile.uri
            }
        }
    }
}