package com.w2sv.datastore.proto.navigatorconfig

import com.w2sv.domain.model.movedestination.LocalDestination
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NavigatorConfigDataSourceImplTest {

    private val localDestinationA =
        LocalDestination.parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs")
    private val localDestinationB =
        LocalDestination.parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots")
    private val localDestinationC =
        LocalDestination.parse("content://com.android.externalstorage.documents/document/primary%3AMoved")

    @Test
    fun testUpdatedQuickMoveDestinations() {
        // When current destinations empty -> destination is added
        assertEquals(
            listOf(localDestinationA),
            updatedQuickMoveDestinations(
                currentDestinations = emptyList(),
                destination = localDestinationA
            )
        )

        // When new destination equals first destination -> list stays as is
        assertEquals(
            listOf(localDestinationA),
            updatedQuickMoveDestinations(
                currentDestinations = listOf(localDestinationA),
                destination = localDestinationA
            )
        )

        // When new destination equals first destination -> list stays as is
        assertEquals(
            listOf(localDestinationA, localDestinationB),
            updatedQuickMoveDestinations(
                currentDestinations = listOf(localDestinationA, localDestinationB),
                destination = localDestinationA
            )
        )

        // When new destination equals second destination -> list gets reversed
        assertEquals(
            listOf(localDestinationB, localDestinationA),
            updatedQuickMoveDestinations(
                currentDestinations = listOf(localDestinationA, localDestinationB),
                destination = localDestinationB
            )
        )

        // When new destination not in list -> second is removed, first becomes second, new becomes first
        assertEquals(
            listOf(localDestinationC, localDestinationA),
            updatedQuickMoveDestinations(
                currentDestinations = listOf(localDestinationA, localDestinationB),
                destination = localDestinationC
            )
        )

        // When new destination not in list -> first becomes second, new becomes first
        assertEquals(
            listOf(localDestinationB, localDestinationA),
            updatedQuickMoveDestinations(
                currentDestinations = listOf(localDestinationA),
                destination = localDestinationB
            )
        )
    }
}
