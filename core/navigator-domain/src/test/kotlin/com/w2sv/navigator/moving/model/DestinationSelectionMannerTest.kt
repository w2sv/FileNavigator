package com.w2sv.navigator.moving.model

import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class DestinationSelectionMannerTest {

    @Test
    fun testParceling() {
        DestinationSelectionManner
            .Picked(NotificationEvent.CancelMoveFile(12))
            .testParceling()

        DestinationSelectionManner
            .Quick(NotificationEvent.CancelMoveFile(19))
            .testParceling()

        DestinationSelectionManner.Auto.testParceling()
    }
}
