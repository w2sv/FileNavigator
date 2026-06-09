package com.w2sv.navigator.notifications.api.controller

import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.notifications.controller.BatchMoveNotificationArgs
import com.w2sv.navigator.notifications.controller.NavigateFileNotificationController
import com.w2sv.navigator.notifications.controller.frequencyOrderedQuickMoveDestinations
import com.w2sv.storage.uri.DocumentUri
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BatchMoveNotificationControllerKtTest {

    @Test
    fun `destinations are ordered by descending frequency`() {
        val dirA = MoveDestination.Directory(DocumentUri.parse("A"))
        val dirB = MoveDestination.Directory(DocumentUri.parse("B"))
        val dirC = MoveDestination.Directory(DocumentUri.parse("C"))

        val args1 = NavigateFileNotificationController.Args(
            navigatableFile = mockk(),
            quickMoveDestinations = listOf(dirA, dirB)
        )

        val args2 = NavigateFileNotificationController.Args(
            navigatableFile = mockk(),
            quickMoveDestinations = listOf(dirA, dirC)
        )

        val args3 = NavigateFileNotificationController.Args(
            navigatableFile = mockk(),
            quickMoveDestinations = listOf(dirA)
        )

        val input: BatchMoveNotificationArgs = mapOf(
            1 to args1,
            2 to args2,
            3 to args3
        )

        assertEquals(
            listOf(
                dirA, // appears 3 times
                dirB, // appears once
                dirC // appears once
            ),
            input.frequencyOrderedQuickMoveDestinations()
        )
    }
}
