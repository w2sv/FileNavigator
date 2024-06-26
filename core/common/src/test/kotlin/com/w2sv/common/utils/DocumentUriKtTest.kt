package com.w2sv.common.utils

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = "AndroidManifest.xml")
class DocumentUriKtTest {

    private val treeDocumentUri: Uri =
        Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AMoved%2FScreenshots/document/primary%3AMoved%2FScreenshots")
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun getDocumentUriPath() {
        assertEquals(
            "primary:Moved/Screenshots",
            DocumentUri(treeDocumentUri).documentFilePath(context)
        )
    }

    @Test
    fun getDocumentUriFileName() {
        assertEquals(null, DocumentUri(treeDocumentUri).documentFile(context)?.name)
    }
}