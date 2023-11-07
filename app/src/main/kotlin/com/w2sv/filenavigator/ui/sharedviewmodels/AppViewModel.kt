package com.w2sv.filenavigator.ui.sharedviewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.data.model.Theme
import com.w2sv.data.storage.preferences.repositories.FileTypeRepository
import com.w2sv.data.storage.preferences.repositories.PreferencesRepository
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.screens.Screen
import com.w2sv.filenavigator.ui.states.StorageAccessState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    fileTypeRepository: FileTypeRepository
) :
    ViewModel() {

    // ==============
    // Permission-related
    // ==============

    val storageAccessState = StorageAccessState(
        priorStatus = preferencesRepository.priorStorageAccess.stateIn(
            viewModelScope,
            SharingStarted.Eagerly
        ),
        setFileTypeStatuses = { fileTypes, status ->
            viewModelScope.launch {
                fileTypeRepository.setFileTypeStatuses(fileTypes, status)
            }
        },
        saveStorageAccessStatus = {
            viewModelScope.launch {
                preferencesRepository.priorStorageAccess.save(it)
            }
        }
    )

    val postNotificationsPermissionRequested =
        preferencesRepository.postNotificationsPermissionRequested.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
        )

    fun savePostNotificationsPermissionRequested() {
        viewModelScope.launch {
            preferencesRepository.postNotificationsPermissionRequested.save(true)
        }
    }

    // ==============
    // Screen
    // ==============

    val screen get() = _screen.asStateFlow()
    private val _screen = MutableStateFlow(Screen.Home)

    fun setScreen(screen: Screen) {
        _screen.value = screen
    }

    // ==============
    // Theme
    // ==============

    val theme = preferencesRepository.theme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
    )

    fun saveTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.theme.save(theme)
        }
    }

    val useDynamicColors = preferencesRepository.useDynamicColors.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
    )

    fun saveUseDynamicColors(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useDynamicColors.save(value)
        }
    }

    // ==============
    // BackPress Handling
    // ==============

    val exitApplication get() = _exitApplication.asSharedFlow()
    private val _exitApplication = MutableSharedFlow<Unit>()

    fun onBackPress(context: Context) {
        if (screen.value == Screen.NavigatorSettings) {
            _screen.value = Screen.Home
        } else {
            backPressHandler.invoke(
                onFirstPress = {
                    context.showToast(context.getString(R.string.tap_again_to_exit))
                },
                onSecondPress = {
                    viewModelScope.launch {
                        _exitApplication.emit(Unit)
                    }
                }
            )
        }
    }

    private val backPressHandler = BackPressHandler(
        viewModelScope,
        2500L
    )
}