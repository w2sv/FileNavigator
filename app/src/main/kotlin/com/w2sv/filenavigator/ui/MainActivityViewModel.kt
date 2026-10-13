package com.w2sv.filenavigator.ui

import androidx.lifecycle.viewModelScope
import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.core.logging.LoggingViewModel
import com.w2sv.domain.repository.AppSettingsRepository
import com.w2sv.domain.repository.RequestFlagRepository
import com.w2sv.kotlinutils.coroutines.flow.mapState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    appSettingsRepository: AppSettingsRepository,
    requestFlagRepository: RequestFlagRepository
) : LoggingViewModel() {

    val postNotificationsPermissionHistory = PermissionRequestHistory(
        wasRequestLaunchedBefore = requestFlagRepository.postNotificationsPermissionRequested.flow,
        recordRequestLaunched = { viewModelScope.launch { requestFlagRepository.postNotificationsPermissionRequested.save(true) } }
    )

    val themeSettings = appSettingsRepository.appSettings.mapState { it.theme }
}
