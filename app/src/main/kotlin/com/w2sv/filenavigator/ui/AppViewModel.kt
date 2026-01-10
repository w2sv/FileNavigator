package com.w2sv.filenavigator.ui

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.common.util.hasManageAllFilesPermission
import com.w2sv.common.util.hasPostNotificationsPermission
import com.w2sv.domain.model.Theme
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.filenavigator.ui.navigation.Screen
import com.w2sv.filenavigator.ui.sharedstate.AppPermissionsState
import com.w2sv.filenavigator.ui.sharedstate.ThemeSettings
import com.w2sv.filenavigator.ui.util.LifecycleLoggingViewModel
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.kotlinutils.coroutines.flow.combineStates
import com.w2sv.kotlinutils.coroutines.flow.mapState
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(private val preferencesRepository: PreferencesRepository, @ApplicationContext context: Context) :
    LifecycleLoggingViewModel() {

    private val postNotificationsPermissionRequested by threadUnsafeLazy {
        preferencesRepository.postNotificationsPermissionRequested.stateIn(
            viewModelScope,
            SharingStarted.Eagerly
        )
    }

    val permissionsState = AppPermissionsState(
        hasPostNotificationsPermission = { context.hasPostNotificationsPermission() },
        hasManageAllFilesPermission = { hasManageAllFilesPermission },
        postNotificationsPermissionRequested = { postNotificationsPermissionRequested.value },
        savePostNotificationsRequested = { viewModelScope.launch { preferencesRepository.postNotificationsPermissionRequested.save(true) } }
    )

    val startScreen: Screen by threadUnsafeLazy {
        when {
            !permissionsState.allGranted.value -> Screen.Permissions
            else -> Screen.Home
        }
    }

    init {
        // Stop FileNavigator when any permission missing
        permissionsState.allGranted.filter { !it }.collectOn(viewModelScope) {
            FileNavigator.stop(context)
        }
    }

    // ==============
    // Theme
    // ==============

    val themeSettings = preferencesRepository.themeSettings(viewModelScope)

    fun saveTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.theme.save(theme)
        }
    }

    fun saveUseAmoledBlackTheme(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.useAmoledBlackTheme.save(value)
        }
    }

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

private fun PreferencesRepository.themeSettings(scope: CoroutineScope): StateFlow<ThemeSettings> =
    combineStates(
        theme.stateIn(scope, SharingStarted.WhileSubscribed()),
        useAmoledBlackTheme.stateIn(scope, SharingStarted.WhileSubscribed()),
        useDynamicColors.stateIn(scope, SharingStarted.WhileSubscribed())
            .mapState { it && dynamicColorsSupported } // TODO: should be done in repository once DataStoreFlow.map is possible
    ) { theme, useAmoledBlackTheme, useDynamicColors ->
        ThemeSettings(
            theme = theme,
            useAmoledBlackTheme = useAmoledBlackTheme,
            useDynamicColors = useDynamicColors
        )
    }
