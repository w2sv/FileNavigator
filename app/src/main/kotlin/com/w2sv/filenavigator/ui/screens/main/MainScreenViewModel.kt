package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import com.w2sv.androidutils.ui.unconfirmed_state.getUnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.getUnconfirmedStatesComposition
import com.w2sv.data.model.FileType
import com.w2sv.data.model.StorageAccessStatus
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.data.storage.repositories.PreferencesRepository
import com.w2sv.filenavigator.ui.utils.getMutableStateMap
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    fun saveDisableNavigatorOnLowBattery(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveDisableNavigatorOnLowBattery(value)
        }
    }

    fun saveShowedManageExternalStorageRational(): Job =
        viewModelScope.launch {
            preferencesRepository.saveShowedManageExternalStorageRational()
        }

    val showedPostNotificationsPermissionsRational by preferencesRepository::showedPostNotificationsPermissionsRational

    fun saveShowedPostNotificationsPermissionsRational(): Job =
        viewModelScope.launch { preferencesRepository.saveShowedPostNotificationsPermissionsRational() }

    val navigatorUIState = NavigatorUIState(
        viewModelScope,
        fileTypeRepository
    )

    val storageAccessState = StorageAccessState(
        priorStatus = preferencesRepository.priorStorageAccessStatus.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            StorageAccessStatus.NoAccess
        ),
        setFileTypeStatuses = navigatorUIState::setFileTypeStatus,
        saveStorageAccessStatus = {
            viewModelScope.launch {
                preferencesRepository.savePriorStorageAccessStatus(it)
            }
        }
    )

    val showManageExternalStorageDialog = combine(
        preferencesRepository.showedManageExternalStorageRational,
        storageAccessState.anyAccessGranted,
    ) { f1, f2 ->
        println("showedManageExternalStorageRational: $f1 anyStorageAccessGranted: $f2")
        !f1 && !f2
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // ==============
    // Navigator Configuration
    // ==============

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