package com.w2sv.navigator.moving

import com.w2sv.domain.model.MoveDestination
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import util.TestInstancesProvider

@RunWith(RobolectricTestRunner::class)
internal class MoveBundleTest {

    @Test
    fun testParceling() {
        MoveBundle(
            file = TestInstancesProvider.moveFile(),
            destination = MoveDestination.parse("lkasjdflkajhlk"),
            mode = MoveMode.DestinationPicked(
                notificationResources = NotificationResources(
                    7,
                    "MoveFileNotificationManager"
                ), isPartOfBatch = true
            )
        )
            .testParceling()
    }
}