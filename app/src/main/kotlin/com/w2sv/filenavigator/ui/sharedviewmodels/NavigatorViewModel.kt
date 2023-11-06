package com.w2sv.filenavigator.ui.sharedviewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.data.storage.preferences.repositories.FileTypeRepository
import com.w2sv.data.storage.preferences.repositories.PreferencesRepository
import com.w2sv.filenavigator.ui.states.FileTypesState
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigatorViewModel @Inject constructor(
    fileTypeRepository: FileTypeRepository,
    private val preferencesRepository: PreferencesRepository,
    @ApplicationContext context: Context
) : ViewModel() {
    val isRunning get() = _isRunning.asStateFlow()

    private val _isRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileNavigator>())

    fun setIsRunning(value: Boolean) {
        _isRunning.value = value
    }

    val startDateTime = preferencesRepository.navigatorStartDateTime.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        null
    )

    val disableOnLowBattery = preferencesRepository.disableNavigatorOnLowBattery.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
    )

    fun saveDisableOnLowBattery(value: Boolean): Job =
        viewModelScope.launch {
            preferencesRepository.disableNavigatorOnLowBattery.save(value)
        }

    val fileTypesState = FileTypesState(
        scope = viewModelScope,
        fileTypeRepository = fileTypeRepository,
        onStateSynced = {
            if (isRunning.value) {
                FileNavigator.reregisterFileObservers(
                    context
                )
            }
        },
    )
}