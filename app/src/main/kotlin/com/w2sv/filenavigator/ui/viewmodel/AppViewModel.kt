package com.w2sv.filenavigator.ui.viewmodel

import android.Manifest
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.hasPermission
import com.w2sv.androidutils.os.postNotificationsPermissionRequired
import com.w2sv.common.util.isExternalStorageManger
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.kotlinutils.coroutines.flow.combineStates
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    val moveDestinationPathConverter: MoveDestinationPathConverter,
    @ApplicationContext context: Context
) :
    ViewModel() {

    // ==============
    // Permission-related
    // ==============

    val manageAllFilesPermissionGranted get() = _manageAllFilesPermissionGranted.asStateFlow()
    private val _manageAllFilesPermissionGranted = MutableStateFlow(isExternalStorageManger)

    fun updateManageAllFilesPermissionGranted() {
        _manageAllFilesPermissionGranted.value = isExternalStorageManger
    }

    private val postNotificationsPermissionGranted =
        MutableStateFlow(
            if (postNotificationsPermissionRequired) {
                context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                true
            }
        )

    fun setPostNotificationsPermissionGranted(value: Boolean) {
        postNotificationsPermissionGranted.value = value
    }

    val postNotificationsPermissionRequested =
        preferencesRepository.postNotificationsPermissionRequested.stateIn(
            viewModelScope,
            SharingStarted.Eagerly
        )

    fun savePostNotificationsPermissionRequestedIfRequired() {
        if (!postNotificationsPermissionRequested.value) {
            viewModelScope.launch {
                preferencesRepository.postNotificationsPermissionRequested.save(true)
            }
        }
    }

    val allPermissionsGranted =
        combineStates(
            postNotificationsPermissionGranted,
            manageAllFilesPermissionGranted
        ) { f1, f2 ->
            f1 && f2
        }

    // ==============
    // Theme
    // ==============

    val theme = preferencesRepository.theme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed()
    )

    fun saveTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.theme.save(theme)
        }
    }

    val useAmoledBlackTheme = preferencesRepository.useAmoledBlackTheme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed()
    )

    fun saveUseAmoledBlackTheme(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useAmoledBlackTheme.save(value)
        }
    }

    val useDynamicColors = preferencesRepository.useDynamicColors.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed()
    )

    fun saveUseDynamicColors(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useDynamicColors.save(value)
        }
    }

    // ==============
    // Other
    // ==============

    val showStorageVolumeNames = preferencesRepository.showStorageVolumeNames.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed()
    )

    fun saveShowStorageVolumeNames(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.showStorageVolumeNames.save(value)
        }
    }

    val showAutoMoveIntroduction = preferencesRepository.showAutoMoveIntroduction.stateIn(
        viewModelScope,
        SharingStarted.Eagerly
    )

    fun saveShowAutoMoveIntroduction(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.showAutoMoveIntroduction.save(value)
        }
    }
}
