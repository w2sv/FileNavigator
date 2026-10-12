package com.w2sv.filenavigator.ui.shared.permissions

import com.w2sv.augmentedpermissions.PermissionRequestHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppPermissionsStateTest {

    private var hasPostNotificationsPermission = false
    private var hasManageAllFilesPermission = false
    private val postNotificationsRequested = MutableStateFlow(false)
    private var saveCallCount = 0

    private lateinit var state: AppPermissionsState

    @Before
    fun setUp() {
        state = AppPermissionsState(
            hasPostNotificationsPermission = { hasPostNotificationsPermission },
            hasManageAllFilesPermission = { hasManageAllFilesPermission },
            postNotificationsPermissionHistory = PermissionRequestHistory(
                wasRequestLaunchedBefore = postNotificationsRequested,
                recordRequestLaunched = {
                    saveCallCount++
                    postNotificationsRequested.value = true
                }
            )
        )
    }

    @Test
    fun `missingPermissions reflects current permission state`() =
        runTest {
            assertEquals(
                RequiredPermission.entries,
                state.missingPermissions.first()
            )
        }

    @Test
    fun `allGranted is true only when no permissions are missing`() =
        runTest {
            hasPostNotificationsPermission = true
            hasManageAllFilesPermission = true

            state.refresh()

            assertTrue(state.allGranted.first())
        }

    @Test
    fun `notification permission history records request`() {
        assertFalse(postNotificationsRequested.value)

        state.postNotificationsPermissionHistory.recordRequestLaunched()

        assertEquals(1, saveCallCount)
        assertTrue(postNotificationsRequested.value)
    }

    @Test
    fun `refresh re-evaluates permission lambdas`() =
        runTest {
            state.refresh()
            assertEquals(2, state.missingPermissions.first().size)

            hasPostNotificationsPermission = true
            hasManageAllFilesPermission = true

            state.refresh()
            assertTrue(state.missingPermissions.first().isEmpty())
        }
}
