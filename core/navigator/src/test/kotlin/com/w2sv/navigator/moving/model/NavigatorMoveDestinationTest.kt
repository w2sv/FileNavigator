package com.w2sv.navigator.moving.model

import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.MediaUri
import com.w2sv.test.testParceling
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class NavigatorMoveDestinationTest {

    @Test
    fun `directory destination parceling`() {
        NavigatorMoveDestination.Directory
            .parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs")
            .testParceling()
    }

    @Test
    fun `local file parceling`() {
        NavigatorMoveDestination.File.Local(
            DocumentUri.parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs%2Fsomefile.gif"),
            MediaUri.parse("content://media/external/images/media/1000012597")
        )
            .testParceling()
    }

    @Test
    fun `external file parceling`() {
        NavigatorMoveDestination.File.External(
            DocumentUri.parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs%2Fsomefile.gif"),
            "some.package.name",
            "Drive"
        )
            .testParceling()
    }
}