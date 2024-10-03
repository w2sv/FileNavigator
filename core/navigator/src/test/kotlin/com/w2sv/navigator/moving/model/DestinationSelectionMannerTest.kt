package com.w2sv.navigator.moving.model

import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class DestinationSelectionMannerTest {

    @Test
    fun testParceling() {
        DestinationSelectionManner
            .Picked(NotificationResources(12, "manager"))
            .testParceling()

        DestinationSelectionManner
            .Quick(NotificationResources(19, "manager"))
            .testParceling()

        DestinationSelectionManner.Auto.testParceling()
    }
}