package com.w2sv.common.util

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath
import com.w2sv.kotlinutils.copyWithReplacedLast
import java.io.File
import kotlinx.parcelize.Parcelize

private const val PATH_SLASH_ENCODING = "%2F"
private const val PATH_COLON_ENCODING = "%3A"

@Parcelize
@JvmInline
value class DocumentUri(val uri: Uri) : Parcelable {

    /**
     * @see DocumentFile.fromSingleUri
     */
    fun documentFile(context: Context): DocumentFile =
        DocumentFile.fromSingleUri(context, uri)!!

    fun documentTreeUri(): DocumentUri =
        uri.run {
            val encodedLastPathSegment = Uri.encode(lastPathSegment)
            parse("content://$authority/tree/$encodedLastPathSegment/document/$encodedLastPathSegment")
        }

    /**
     * Does not depend on the file corresponding to [uri] being present.
     *
     * @return e.g. __primary:Moved/Screenshots__ for [uri]=__content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots__.
     */
    fun documentFilePath(context: Context): String =
        documentFile(context).getSimplePath(context)

    fun mediaUri(context: Context): MediaUri? =
        MediaUri.fromDocumentUri(context, this)

    fun childDocumentUri(fileName: String): DocumentUri =
        parse(uri.toString() + PATH_SLASH_ENCODING + Uri.encode(fileName))

    val isVolumeRoot: Boolean
        get() = uri.toString().endsWith("$PATH_COLON_ENCODING$PATH_SLASH_ENCODING")

    val volumeName: String
        get() = uri.toString().substringBefore(PATH_COLON_ENCODING).substringAfterLast("/")

    val parent: DocumentUri?
        get() = uri
            .toString()
            .split(PATH_COLON_ENCODING)
            .let { colonSplitSegments ->
                colonSplitSegments
                    .last()
                    .let { postColonSegment ->
                        if (postColonSegment == PATH_SLASH_ENCODING) {
                            null
                        } else {
                            postColonSegment
                                .substringBeforeLast(
                                    PATH_SLASH_ENCODING,
                                    missingDelimiterValue = ""
                                )
                                .let { parentPath ->
                                    parse(
                                        colonSplitSegments
                                            .copyWithReplacedLast(parentPath.ifEmpty { PATH_SLASH_ENCODING })
                                            .joinToString(PATH_COLON_ENCODING)
                                    )
                                }
                        }
                    }
            }

    companion object {
        fun parse(uriString: String): DocumentUri =
            uriString.toUri().documentUri

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

val Uri.decoded: String
    get() = Uri.decode(toString())

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
