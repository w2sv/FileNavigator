package com.w2sv.common.utils

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
@JvmInline
value class DocumentUri(val uri: Uri) : Parcelable {

    fun isValidDocumentUri(context: Context): Boolean =
        DocumentFile.isDocumentUri(context, uri)

    fun documentFile(context: Context): DocumentFile? =
        DocumentFile.fromSingleUri(context, uri)

    /**
     * Returns e.g. "primary:Moved/Screenshots" for [uri]="content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots".
     *
     * Does not depend on the file corresponding to [uri] being present.
     */
    fun documentFilePath(context: Context): String? =
        documentFile(context)?.getSimplePath(context)

    fun mediaUri(context: Context): MediaUri? =
        MediaUri.fromDocumentUri(context, this)

    fun childDocumentUri(fileName: String): DocumentUri =
        parse("$uri%2F${Uri.encode(fileName)}")

    override fun toString(): String =
        uri.toString()

    companion object {
        fun parse(uriString: String): DocumentUri =
            DocumentUri(Uri.parse(uriString))

        fun fromDocumentFile(documentFile: DocumentFile): DocumentUri =
            DocumentUri(documentFile.uri)

        fun fromTreeUri(context: Context, treeUri: Uri): DocumentUri? =
            DocumentFile.fromTreeUri(context, treeUri)?.let { fromDocumentFile(it) }

        fun fromMediaUri(context: Context, mediaUri: MediaUri): DocumentUri? =
            mediaUri.documentUri(context)

        fun fromFile(file: File): DocumentUri =
            fromDocumentFile(DocumentFile.fromFile(file))
    }
}