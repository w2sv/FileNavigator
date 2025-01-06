package com.w2sv.common.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.w2sv.test.testParceling
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = "AndroidManifest.xml")
internal class DocumentUriTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private val contentUri: DocumentUri =
        DocumentUri.parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs")
    private val treeDocumentUri: DocumentUri =
        DocumentUri.parse(
            "content://com.android.externalstorage.documents/tree/primary%3AMoved%2FScreenshots/document/primary%3AMoved%2FScreenshots"
        )

    @Test
    fun testParceling() {
        contentUri.testParceling()
    }

    @Test
    fun testFromTreeUri() {
        assertEquals(
            treeDocumentUri,
            DocumentUri.fromTreeUri(context, treeDocumentUri.uri)
        )
    }

    @Test
    fun testDocumentTreeUri() {
        assertEquals(
            DocumentUri.parse(
                "content://com.android.externalstorage.documents/tree/primary%3AMoved%2FGIFs/document/primary%3AMoved%2FGIFs"
            ),
            contentUri.documentTreeUri()
        )
    }

    @Test
    fun testDocumentFilePath() {
        assertEquals(
            "primary:Moved/Screenshots",
            treeDocumentUri.documentFilePath(context)
        )
        assertEquals(
            "primary:Moved/GIFs",
            contentUri.documentFilePath(context)
        )
    }

    @Test
    fun testDocumentFileName() {
        assertEquals(null, treeDocumentUri.documentFile(context).name)
    }

    @Test
    fun testChildDocumentUri() {
        assertEquals(
            DocumentUri.parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs%2FsomeFile.jpg"),
            contentUri.childDocumentUri("someFile.jpg")
        )
    }

    @Test
    fun testParent() {
        assertEquals(
            DocumentUri.parse("content://com.android.externalstorage.documents/document/primary%3AMoved"),
            contentUri.parent
        )
        assertEquals(
            DocumentUri.parse("content://com.android.externalstorage.documents/document/primary%3A%2F"),
            contentUri.parent?.parent
        )
        assertEquals(
            null,
            contentUri.parent?.parent?.parent
        )
    }

    @Test
    fun testVolumeName() {
        assertEquals(
            "primary",
            contentUri.volumeName
        )
    }
}
