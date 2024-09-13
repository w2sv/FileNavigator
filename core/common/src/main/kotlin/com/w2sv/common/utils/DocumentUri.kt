package com.w2sv.common.utils

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath
import kotlinx.parcelize.Parcelize
import java.io.File

private const val PATH_SLASH_ENCODING = "%2F"
//private const val PATH_COLON_ENCODING = "%3A"

@Parcelize
@JvmInline
value class DocumentUri(val uri: Uri) : Parcelable {

    /**
     * @see DocumentFile.fromSingleUri
     */
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
        parse(uri.toString() + PATH_SLASH_ENCODING + Uri.encode(fileName))

    val parent: DocumentUri?
        get() = toString()
            .substringBeforeLast(PATH_SLASH_ENCODING, missingDelimiterValue = "")
            .run {
                if (isEmpty()) {
                    null
                } else {
                    parse(this)
                }
            }

    companion object {
        fun parse(uriString: String): DocumentUri =
            Uri.parse(uriString).documentUri

        fun fromDocumentFile(documentFile: DocumentFile): DocumentUri =
            documentFile.uri.documentUri

        fun fromTreeUri(context: Context, treeUri: Uri): DocumentUri? =
            DocumentFile.fromTreeUri(context, treeUri)?.let { fromDocumentFile(it) }

        fun fromMediaUri(context: Context, mediaUri: MediaUri): DocumentUri? =
            mediaUri.documentUri(context)

        fun fromFile(file: File): DocumentUri =
            fromDocumentFile(DocumentFile.fromFile(file))
    }
}

val Uri.documentUri: DocumentUri
    get() = DocumentUri(this)

fun Uri.documentUriIfValid(context: Context): DocumentUri? {
    return if (isValidDocumentUri(context)) {
        documentUri
    } else {
        null
    }
}

fun Uri.isValidDocumentUri(context: Context): Boolean =
    DocumentFile.isDocumentUri(context, this)