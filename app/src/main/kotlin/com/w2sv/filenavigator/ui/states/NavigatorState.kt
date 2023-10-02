package com.w2sv.filenavigator.ui.states

import android.content.Context
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.data.storage.repositories.PreferencesRepository
import com.w2sv.navigator.FileNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NavigatorState(
    private val scope: CoroutineScope,
    fileTypeRepository: FileTypeRepository,
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

    fun saveDisableOnLowBattery(value: Boolean): Job =
        scope.launch {
            preferencesRepository.disableNavigatorOnLowBattery.save(value)
        }

    val fileTypesState = FileTypesState(
        scope = scope,
        fileTypeRepository = fileTypeRepository,
        onStateSynced = {
            if (isRunning.value) {
                FileNavigator.reregisterFileObservers(
                    context
                )
            }
        },
        defaultMoveDestinationState = DefaultMoveDestinationState(
            fileTypeRepository = fileTypeRepository,
            scope = scope,
            context = context
        )
    )
}