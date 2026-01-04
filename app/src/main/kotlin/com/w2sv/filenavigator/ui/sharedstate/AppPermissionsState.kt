package com.w2sv.filenavigator.ui.sharedstate

import com.w2sv.kotlinutils.coroutines.flow.mapState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class RequiredPermission {
    ManageAllFiles,
    PostNotifications
}

/**
 * Holds and derives the current state of all required app permissions.
 *
 * The state is exposed as a set of [RequiredPermission]s that are currently missing,
 * allowing the UI to render permission requests declaratively.
 */
class AppPermissionsState(
    private val hasPostNotificationsPermission: () -> Boolean,
    private val hasManageAllFilesPermission: () -> Boolean,
    val postNotificationsPermissionRequested: () -> Boolean,
    private val savePostNotificationsRequested: () -> Unit
) {
    val missingPermissions: StateFlow<List<RequiredPermission>>
        field = MutableStateFlow(computeMissingPermissions())

    val allGranted = missingPermissions.mapState { it.isEmpty() }

    private fun computeMissingPermissions(): List<RequiredPermission> =
        RequiredPermission.entries.filterNot {
            when (it) {
                RequiredPermission.ManageAllFiles -> hasManageAllFilesPermission()
                RequiredPermission.PostNotifications -> hasPostNotificationsPermission()
            }
        }

    fun onPostNotificationsPermissionRequested() {
        if (!postNotificationsPermissionRequested()) {
            savePostNotificationsRequested()
        }
    }

    /**
     * Refreshes the current permission state by re-evaluating
     * [hasPostNotificationsPermission] and [hasManageAllFilesPermission].
     *
     * This should be called if the permissions may have changed outside the app
     * (for example, via system settings).
     */
    fun refresh() {
        missingPermissions.value = computeMissingPermissions()
    }
}
