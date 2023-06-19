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
import com.w2sv.filenavigator.utils.StorageAccessStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import slimber.log.i
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

    val inAppTheme = makeUnconfirmedEnumStateFlow(
        dataStoreRepository.inAppTheme,
        PreferencesKey.IN_APP_THEME
    )

    val unconfirmedExtendedSettings =
        makeUnconfirmedStatesComposition(listOf(disableListenerOnLowBattery, inAppTheme))

    // ==============
    // StorageAccessStatus
    // ==============

    val storageAccessStatus: StateFlow<StorageAccessStatus> get() = _storageAccessStatus
    private val _storageAccessStatus = MutableStateFlow(StorageAccessStatus.NoAccess)

    fun updateStorageAccessStatus(context: Context) {
        _storageAccessStatus.value = StorageAccessStatus.get(context)
            .also { status ->
                val previousStatus = repository.previousStorageAccessStatus.getValueSynchronously()

                if (status != previousStatus) {
                    i { "New manageExternalStoragePermissionGranted = $status diverting from previous = $previousStatus" }

                    when (status) {
                        StorageAccessStatus.NoAccess -> setFileTypeStatuses(
                            FileType.all,
                            FileType.Status.DisabledForNoFileAccess
                        )

                        StorageAccessStatus.MediaFilesOnly -> {
                            setFileTypeStatuses(
                                FileType.NonMedia.all,
                                FileType.Status.DisabledForMediaAccessOnly
                            )

                            if (previousStatus == StorageAccessStatus.NoAccess) {
                                setFileTypeStatuses(FileType.Media.all, FileType.Status.Enabled)
                            }
                        }

                        StorageAccessStatus.AllFiles -> setFileTypeStatuses(
                            FileType.all,
                            FileType.Status.Enabled
                        )
                    }

                    coroutineScope.launch {
                        repository.save(
                            PreferencesKey.PREVIOUS_STORAGE_ACCESS_STATUS,
                            status
                        )
                    }
                }
            }
    }

    private fun setFileTypeStatuses(fileTypes: Iterable<FileType>, newStatus: FileType.Status) {
        coroutineScope.launch {
            repository.saveEnumValuedMap(
                fileTypes.associateWith { newStatus }
            )
            fileTypes.forEach {
                fileTypeStatus[it] = newStatus
            }
        }
    }

    // ==============
    // Navigator Configuration
    // ==============

    val fileTypeStatus by lazy {
        makeUnconfirmedEnumValuedStateMap(dataStoreRepository.fileTypeStatus)
    }

    val fileSourceEnabled by lazy {
        makeUnconfirmedStateMap(dataStoreRepository.fileSourceEnabled)
    }

    val unconfirmedNavigatorConfiguration by lazy {
        makeUnconfirmedStatesComposition(listOf(fileTypeStatus, fileSourceEnabled))
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