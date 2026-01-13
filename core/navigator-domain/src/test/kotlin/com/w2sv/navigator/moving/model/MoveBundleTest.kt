package com.w2sv.navigator.moving.model

import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import util.TestInstance

@RunWith(RobolectricTestRunner::class)
internal class MoveBundleTest {

    @Test
    fun testParceling() {
        MoveOperation.DirectoryDestinationPicked(
            file = TestInstance.moveFile(),
            destination = MoveDestination.Directory.parse("lkasjdflkajhlk"),
            destinationSelectionManner = DestinationSelectionManner.Picked(NotificationEvent.CancelMoveFile(7)),
            isPartOfBatch = true
        )
            .testParceling()
    }
}
