package com.w2sv.filenavigator.ui

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.core.logging.LoggingViewModel
import com.w2sv.core.util.hasManageAllFilesPermission
import com.w2sv.core.util.hasPostNotificationsPermission
import com.w2sv.domain.repository.AppSettingsRepository
import com.w2sv.domain.repository.RequestFlagRepository
import com.w2sv.filenavigator.ui.navigation.main.MainNavRoute
import com.w2sv.filenavigator.ui.shared.permissions.AppPermissionsState
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.kotlinutils.coroutines.flow.mapState
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.FileNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    appSettingsRepository: AppSettingsRepository,
    requestFlagRepository: RequestFlagRepository,
    @ApplicationContext context: Context
) : LoggingViewModel() {

    val permissionsState = AppPermissionsState(
        hasPostNotificationsPermission = { context.hasPostNotificationsPermission() },
        hasManageAllFilesPermission = { hasManageAllFilesPermission },
        postNotificationsPermissionHistory = PermissionRequestHistory(
            wasRequestLaunchedBefore = requestFlagRepository.postNotificationsPermissionRequested.flow,
            recordRequestLaunched = { viewModelScope.launch { requestFlagRepository.postNotificationsPermissionRequested.save(true) } }
        )
    )

    val startRoute: MainNavRoute by threadUnsafeLazy {
        when {
            !permissionsState.allGranted.value -> MainNavRoute.Permissions
            else -> MainNavRoute.BottomNavHost()
        }
    }

    val themeSettings = appSettingsRepository.appSettings.mapState { it.theme }

    init {
        // Stop FileNavigator when any permission missing
        permissionsState.allGranted.filter { !it }.collectOn(viewModelScope) {
            FileNavigator.stop(context)
        }
    }
}
