package com.w2sv.filesystem

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class DocumentUri(val uri: Uri) : Parcelable {

    fun isValidDocumentUri(context: Context): Boolean =
        DocumentFile.isDocumentUri(context, uri)

    fun documentFile(context: Context): DocumentFile =
        DocumentFile.fromSingleUri(context, uri)!!

    /**
     * Returns e.g. "primary:Moved/Screenshots" for [uri]="content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots".
     *
     * Does not depend on the file corresponding to [uri] being present.
     */
    fun documentFilePath(context: Context): String =
        documentFile(context).getSimplePath(context)
}

/**
 * Returns e.g. "primary:Moved/Screenshots" for [documentUri]="content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots".
 *
 * Does not depend on the file corresponding to [documentUri] being present.
 */
fun getDocumentUriPath(documentUri: Uri, context: Context): String =
    DocumentFile.fromSingleUri(context, documentUri)!!.getSimplePath(context)

/**
 * Returns null if file corresponding to [documentUri] not present.
 */
fun getDocumentUriFileName(documentUri: Uri, context: Context): String? =
    DocumentFile.fromSingleUri(context, documentUri)?.name