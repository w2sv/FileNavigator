package com.w2sv.filenavigator.ui.state

import android.Manifest
import android.content.Context
import com.w2sv.androidutils.hasPermission
import com.w2sv.androidutils.os.postNotificationsPermissionRequired
import com.w2sv.common.util.isExternalStorageManger
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.coroutines.flow.combineStates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppPermissions(
    private val preferencesRepository: PreferencesRepository,
    private val coroutineScope: CoroutineScope,
    context: Context
) {
    val manageAllFilesGranted: StateFlow<Boolean> get() = _manageAllFilesGranted
    private val _manageAllFilesGranted = MutableStateFlow(isExternalStorageManger)

    fun updateManageAllFilesPermission() {
        _manageAllFilesGranted.value = isExternalStorageManger
    }

    val postNotificationsGranted: StateFlow<Boolean> get() = _postNotificationsGranted
    private val _postNotificationsGranted = MutableStateFlow(
        !postNotificationsPermissionRequired || context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
    )

    fun setPostNotificationsGranted(value: Boolean) {
        _postNotificationsGranted.value = value
    }

    val postNotificationsRequested: StateFlow<Boolean> =
        preferencesRepository.postNotificationsPermissionRequested.stateIn(
            coroutineScope,
            SharingStarted.Eagerly
        )

    fun savePostNotificationsRequested() {
        if (!postNotificationsRequested.value) {
            coroutineScope.launch {
                preferencesRepository.postNotificationsPermissionRequested.save(true)
            }
        }
    }

    val anyMissing: StateFlow<Boolean> = combineStates(
        listOf(postNotificationsGranted, manageAllFilesGranted)
    ) { permissions -> permissions.any { !it } }
}
