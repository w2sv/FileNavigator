package com.w2sv.filenavigator.ui.sharedstate

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppPermissionsStateTest {

    private var hasPostNotificationsPermission = false
    private var hasManageAllFilesPermission = false
    private var postNotificationsRequested = false
    private var saveCallCount = 0

    private lateinit var state: AppPermissionsState

    @Before
    fun setUp() {
        state = AppPermissionsState(
            hasPostNotificationsPermission = { hasPostNotificationsPermission },
            hasManageAllFilesPermission = { hasManageAllFilesPermission },
            postNotificationsPermissionRequested = { postNotificationsRequested },
            savePostNotificationsRequested = {
                saveCallCount++
                postNotificationsRequested = true
            }
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
    fun `onPostNotificationsPermissionRequested persists request`() {
        assertFalse(postNotificationsRequested)

        state.onPostNotificationsPermissionRequested()
        state.onPostNotificationsPermissionRequested()

        assertEquals(1, saveCallCount)
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
