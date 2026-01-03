package com.w2sv.filenavigator.ui.sharedstate

import com.w2sv.kotlinutils.coroutines.flow.combineStates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds the current state of required app permissions and provides methods to update and track them.
 */
class AppPermissionsState(
    private val hasPostNotificationsPermission: () -> Boolean,
    private val hasManageAllFilesPermission: () -> Boolean,
    val postNotificationsPermissionRequested: () -> Boolean,
    private val savePostNotificationsRequested: () -> Unit
) {
    val manageAllFilesPermissionGranted: StateFlow<Boolean>
        field = MutableStateFlow(hasManageAllFilesPermission())

    val postNotificationsPermissionGranted: StateFlow<Boolean>
        field = MutableStateFlow(hasPostNotificationsPermission())

    fun setPostNotificationsPermissionGranted(value: Boolean) {
        postNotificationsPermissionGranted.value = value
    }

    fun onPostNotificationsPermissionRequested() {
        if (!postNotificationsPermissionRequested()) {
            savePostNotificationsRequested()
        }
    }

    val allGranted = combineStates(postNotificationsPermissionGranted, manageAllFilesPermissionGranted) { a, b -> a && b }

    /**
     * Refreshes the current permission state by re-evaluating the
     * [postNotificationsPermissionGranted] and [manageAllFilesPermissionGranted] flags
     * using the provided lambdas.
     *
     * This should be called if the permissions may have changed outside the app
     * (for example, via system settings).
     */
    fun refresh() {
        postNotificationsPermissionGranted.value = hasPostNotificationsPermission()
        manageAllFilesPermissionGranted.value = hasManageAllFilesPermission()
    }
}
