package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.DataStoreRepository
import com.w2sv.filenavigator.datastore.PreferencesKey
import com.w2sv.filenavigator.service.FileListenerService
import com.w2sv.filenavigator.ui.UnconfirmedStatesHoldingViewModel
import com.w2sv.filenavigator.utils.isExternalStorageManger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    dataStoreRepository: DataStoreRepository
) : UnconfirmedStatesHoldingViewModel<DataStoreRepository>(dataStoreRepository) {

    val isListenerRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileListenerService>())

    val snackbarHostState: SnackbarHostState = SnackbarHostState()

    val disableListenerOnLowBattery = makeUnconfirmedStateFlow(
        dataStoreRepository.disableListenerOnLowBattery,
        PreferencesKey.DISABLE_LISTENER_ON_LOW_BATTERY
    )

    // ==============
    // manageExternalStoragePermissionGranted
    // ==============

    val manageExternalStoragePermissionGranted = MutableStateFlow(false)

    fun updateManageExternalStoragePermissionGranted() {
        manageExternalStoragePermissionGranted.value = isExternalStorageManger()
    }

    // ==============
    // Listener Configuration
    // ==============

    val accountForFileType by lazy {
        makeUnconfirmedStateMap(dataStoreRepository.accountForFileType)
    }

    val accountForFileTypeSource by lazy {
        makeUnconfirmedStateMap(dataStoreRepository.accountForFileTypeSource)
    }

    val unconfirmedListenerConfiguration by lazy {
        makeUnconfirmedStatesComposition(listOf(accountForFileType, accountForFileTypeSource))
    }

    // ==============
    // BackPress Handling
    // ==============

    val exitApplication = MutableSharedFlow<Unit>()

    fun onBackPress(context: Context) {
        backPressHandler.invoke(
            onFirstPress = {
                context.showToast(context.getString(R.string.tap_again_to_exit))
            },
            onSecondPress = {
                viewModelScope.launch {
                    exitApplication.emit(Unit)
                }
            }
        )
    }

    private val backPressHandler = BackPressHandler(
        viewModelScope,
        2500L
    )
}