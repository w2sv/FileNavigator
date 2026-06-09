package com.w2sv.filenavigator.ui

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.w2sv.common.util.hasManageAllFilesPermission
import com.w2sv.common.util.hasPostNotificationsPermission
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.filenavigator.ui.navigation.Screen
import com.w2sv.filenavigator.ui.sharedstate.AppPermissionsState
import com.w2sv.filenavigator.ui.util.LoggingViewModel
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.kotlinutils.coroutines.flow.mapState
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@HiltViewModel
class AppShellViewModel @Inject constructor(preferencesRepository: PreferencesRepository, @ApplicationContext context: Context) :
    LoggingViewModel() {

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

    val themeSettings = preferencesRepository.appSettings.mapState { it.theme }

    init {
        // Stop FileNavigator when any permission missing
        permissionsState.allGranted.filter { !it }.collectOn(viewModelScope) {
            FileNavigator.stop(context)
        }
    }
}
