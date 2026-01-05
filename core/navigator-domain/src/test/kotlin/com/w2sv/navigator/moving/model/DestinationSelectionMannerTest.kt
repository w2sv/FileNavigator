package com.w2sv.navigator.moving.model

import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class DestinationSelectionMannerTest {

    @Test
    fun testParceling() {
        DestinationSelectionManner
            .Picked(CancelNotificationParams(12, "manager"))
            .testParceling()

        DestinationSelectionManner
            .Quick(CancelNotificationParams(19, "manager"))
            .testParceling()

        DestinationSelectionManner.Auto.testParceling()
    }
}
