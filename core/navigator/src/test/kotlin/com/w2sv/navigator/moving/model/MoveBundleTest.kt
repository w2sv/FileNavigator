package com.w2sv.navigator.moving.model

import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import util.TestInstance

@RunWith(RobolectricTestRunner::class)
internal class MoveBundleTest {

    @Test
    fun testParceling() {
        MoveBundle.DirectoryDestinationPicked(
            file = TestInstance.moveFile(),
            destination = NavigatorMoveDestination.Directory.parse("lkasjdflkajhlk"),
            destinationSelectionManner = DestinationSelectionManner.Picked(
                NotificationResources(
                    7,
                    "MoveFileNotificationManager"
                )
            ),
            batched = true
        )
            .testParceling()
    }
}
