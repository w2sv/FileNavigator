package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.data.storage.repositories.PreferencesRepository
import com.w2sv.filenavigator.ui.states.NavigatorState
import com.w2sv.filenavigator.ui.states.StorageAccessState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    fileTypeRepository: FileTypeRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val navigatorState = NavigatorState(
        viewModelScope,
        fileTypeRepository,
        preferencesRepository,
        context
    )

    val storageAccessState = StorageAccessState(
        priorStatus = preferencesRepository.priorStorageAccessStatus.stateIn(
            viewModelScope,
            SharingStarted.Eagerly
        ),
        setFileTypeStatuses = navigatorState.fileTypeState::setAndApplyStatus,
        saveStorageAccessStatus = {
            viewModelScope.launch {
                preferencesRepository.priorStorageAccessStatus.save(it)
            }
        }
    )

    val postNotificationsPermissionRequested =
        preferencesRepository.postNotificationsPermissionRequested.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
        )

    fun savePostNotificationsPermissionRequested() {
        viewModelScope.launch {
            preferencesRepository.postNotificationsPermissionRequested.save(true)
        }
    }
}