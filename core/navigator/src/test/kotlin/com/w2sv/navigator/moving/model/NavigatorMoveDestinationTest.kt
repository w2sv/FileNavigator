package com.w2sv.navigator.moving.model

import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.MediaUri
import com.w2sv.test.testParceling
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class NavigatorMoveDestinationTest {

    @Test
    fun `directory destination parceling`() {
        MoveDestination.Directory
            .parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs")
            .testParceling()
    }

    @Test
    fun `local file parceling`() {
        MoveDestination.File.Local(
            DocumentUri.parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs%2Fsomefile.gif"),
            MediaUri.parse("content://media/external/images/media/1000012597")
        )
            .testParceling()
    }

    @Test
    fun `external file parceling`() {
        MoveDestination.File.External(
            DocumentUri.parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs%2Fsomefile.gif"),
            "some.package.name",
            "Drive"
        )
            .testParceling()
    }

    @Test
    fun `Directory equals working correctly`() {
        val directory = MoveDestination.Directory
            .parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs")
        val equalDirectory = MoveDestination.Directory
            .parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs")
        val differentDirectory = MoveDestination.Directory
            .parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots")

        assertTrue(directory == equalDirectory)
        assertTrue(directory != differentDirectory)
        assertFalse(directory == differentDirectory)
    }
}
