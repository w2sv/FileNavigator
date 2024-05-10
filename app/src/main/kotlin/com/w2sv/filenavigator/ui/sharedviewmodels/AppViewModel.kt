package com.w2sv.filenavigator.ui.sharedviewmodels

import android.Manifest
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.coroutines.combineStates
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.common.utils.postNotificationsPermissionRequired
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.screens.Screen
import com.w2sv.filenavigator.ui.utils.BACK_PRESS_WINDOW_DURATION
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext context: Context
) :
    ViewModel() {

    // ==============
    // Permission-related
    // ==============

    val manageAllFilesPermissionGranted get() = _manageAllFilesPermissionGranted.asStateFlow()
    private val _manageAllFilesPermissionGranted = MutableStateFlow(isExternalStorageManger())

    fun updateManageAllFilesPermissionGranted(): Boolean =
        isExternalStorageManger().also { _manageAllFilesPermissionGranted.value = it }

    private val _postNotificationsPermissionGranted =
        MutableStateFlow(
            if (postNotificationsPermissionRequired())
                context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
            else
                true
        )

    fun setPostNotificationsPermissionGranted(value: Boolean) {
        _postNotificationsPermissionGranted.value = value
    }

    val postNotificationsPermissionRequested =
        preferencesRepository.postNotificationsPermissionRequested.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
        )

    fun savePostNotificationsPermissionRequestedIfRequired() {
        if (!postNotificationsPermissionRequested.value) {
            viewModelScope.launch {
                preferencesRepository.postNotificationsPermissionRequested.save(true)
            }
        }
    }

    private val allPermissionsGranted =
        combineStates(
            _postNotificationsPermissionGranted,
            manageAllFilesPermissionGranted,
        ) { f1, f2 ->
            f1 && f2
        }

    // ==============
    // Screen
    // ==============

    val screen get() = _screen.asStateFlow()
    private val _screen =
        MutableStateFlow(if (allPermissionsGranted.value) Screen.Home else Screen.MissingPermissions)

    fun setScreen(screen: Screen) {
        _screen.value = screen
    }

    init {
        viewModelScope.collectFromFlow(allPermissionsGranted) {
            setScreen(if (it) Screen.Home else Screen.MissingPermissions)
        }
    }

    // ==============
    // Theme
    // ==============

    val theme = preferencesRepository.theme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
    )

    fun saveTheme(theme: com.w2sv.domain.model.Theme) {
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
}