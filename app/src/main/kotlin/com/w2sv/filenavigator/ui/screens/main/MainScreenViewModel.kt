package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.DataStoreRepository
import com.w2sv.filenavigator.datastore.PreferencesKey
import com.w2sv.filenavigator.mediastore.FileType
import com.w2sv.filenavigator.service.FileNavigatorService
import com.w2sv.filenavigator.ui.UnconfirmedStatesHoldingViewModel
import com.w2sv.filenavigator.utils.isExternalStorageManger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    dataStoreRepository: DataStoreRepository
) : UnconfirmedStatesHoldingViewModel<DataStoreRepository>(dataStoreRepository) {

    val isNavigatorRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileNavigatorService>())

    val snackbarHostState: SnackbarHostState = SnackbarHostState()

    val disableListenerOnLowBattery = makeUnconfirmedStateFlow(
        dataStoreRepository.disableListenerOnLowBattery,
        PreferencesKey.DISABLE_LISTENER_ON_LOW_BATTERY
    )

    // ==============
    // manageExternalStoragePermissionGranted
    // ==============

    val manageExternalStoragePermissionGranted: StateFlow<Boolean> get() = _manageExternalStoragePermissionGranted
    private val _manageExternalStoragePermissionGranted = MutableStateFlow(false)

    fun updateManageExternalStoragePermissionGranted() {
        _manageExternalStoragePermissionGranted.value = isExternalStorageManger()
//            .also {
//                if (it != repository.manageExternalStoragePermissionPreviouslyGranted.getValueSynchronously()){
//                    if (it){
//
//                    }
//                }
//                if (!it && FileType.NonMedia.all.any { nonMediaFileType ->
//                        fileTypeEnabled.getValue(
//                            nonMediaFileType
//                        )
//                    }) {
//                    coroutineScope.launch {
//                        repository.saveMap(
//                            FileType.NonMedia.all.associateWith { false }
//                        )
//                    }
//                }
//            }
    }

    // ==============
    // Navigator Configuration
    // ==============

    val fileTypeEnabled by lazy {
        makeUnconfirmedStateMap(dataStoreRepository.fileTypeEnabled)
    }

    val fileSourceEnabled by lazy {
        makeUnconfirmedStateMap(dataStoreRepository.fileSourceEnabled)
    }

    val unconfirmedNavigatorConfiguration by lazy {
        makeUnconfirmedStatesComposition(listOf(fileTypeEnabled, fileSourceEnabled))
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