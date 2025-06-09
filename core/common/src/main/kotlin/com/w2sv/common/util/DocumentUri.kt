package com.w2sv.common.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.provider.DocumentsContract
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.isDocumentTreeUri
import com.anggrayudi.storage.file.StorageType
import com.anggrayudi.storage.file.getSimplePath
import com.w2sv.kotlinutils.copyWithReplacedLast
import kotlinx.parcelize.Parcelize
import java.io.File

private const val SLASH_ENCODING = "%2F"
private const val COLON_ENCODING = "%3A"

@Parcelize
@JvmInline
value class DocumentUri(val uri: Uri) : Parcelable {

    /**
     * @see DocumentsContract.getDocumentId
     */
    val documentId: String
        get() = DocumentsContract.getDocumentId(uri)

    // ====================
    // DocumentFile-related
    // ====================

    /**
     * Invokes [DocumentFile.fromTreeUri] if [isDocumentTreeUri] returns true, otherwise [DocumentFile.fromSingleUri].
     */
    fun documentFile(context: Context): DocumentFile =
        if (isDocumentTreeUri) {
            DocumentFile.fromTreeUri(context, uri)
        } else {
            DocumentFile.fromSingleUri(context, uri)
        }!!

    /**
     * Does not depend on the file corresponding to [uri] being present.
     *
     * @return e.g. __primary:Moved/Screenshots__ for [uri]=__content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots__.
     */
    fun documentFilePath(context: Context): String =
        documentFile(context).getSimplePath(context)

    fun fileName(context: Context): String? {
        val filePath = documentFilePath(context)
        return filePath.substringAfterLast("/").orNullIf { it == filePath }
    }

    // ====================
    // DocumentTreeUri-related
    // ====================

    /**
     * Checks whether this uri is a tree document uri by checking whether its [Uri.getPath] starts with `/tree/`.
     */
    val isDocumentTreeUri: Boolean
        get() = uri.isDocumentTreeUri

    /**
     * If [isDocumentTreeUri] returns false, converts this document Uri to a document tree uri by manually converting to its structure, being
     * ```[SCHEME]://[AUTHORITY]/tree/[PATH]/document/[PATH]```
     */
    fun documentTreeUri(): DocumentUri =
        if (isDocumentTreeUri)
            this
        else
            uri.run {
                val encodedLastPathSegment = Uri.encode(lastPathSegment)
                parse("content://$authority/tree/$encodedLastPathSegment/document/$encodedLastPathSegment")
            }

    /**
     * Converts this documentTreeUri to a documentUri by manually removing the tree path segment, and thereby converting it to the single file
     * document uri structure. If [isDocumentTreeUri] returns false, however, this Uri is returned as is.
     */
    fun treePathSegmentRemoved(): DocumentUri =
        if (isDocumentTreeUri)
            uri.run {
                buildUpon()
                    .path(path!!.replaceBefore("/document/", ""))
                    .build()
                    .documentUri
            }
        else
            this

    // ====================
    // Volumes
    // ====================

    /**
     * @return `true` if this Uri points to a volume root.
     */
    val isVolumeRoot: Boolean
        get() = uri.toString().endsWith("$COLON_ENCODING$SLASH_ENCODING")

    val volumeName: String
        get() = uri.toString().substringBefore(COLON_ENCODING).substringAfterLast("/")

    val storageType: StorageType
        get() = StorageType.fromStorageId(volumeName)

    // ====================
    // Child/Parent Relationships
    // ====================

    val parent: DocumentUri?
        get() = uri
            .toString()
            .split(COLON_ENCODING)
            .let { colonSplitSegments ->
                colonSplitSegments
                    .last()
                    .let { postColonSegment ->
                        if (postColonSegment == SLASH_ENCODING) {
                            null
                        } else {
                            postColonSegment
                                .substringBeforeLast(
                                    SLASH_ENCODING,
                                    missingDelimiterValue = ""
                                )
                                .let { parentPath ->
                                    parse(
                                        colonSplitSegments
                                            .copyWithReplacedLast(parentPath.ifEmpty { SLASH_ENCODING })
                                            .joinToString(COLON_ENCODING)
                                    )
                                }
                        }
                    }
            }

    fun constructChildDocumentUri(fileName: String): DocumentUri =
        parse(uri.toString() + SLASH_ENCODING + Uri.encode(fileName))

    fun isParentOf(child: DocumentUri, contentResolver: ContentResolver): Boolean =
        DocumentsContract.isChildDocument(contentResolver, uri, child.uri)

    fun isChildOf(parent: DocumentUri, contentResolver: ContentResolver): Boolean =
        DocumentsContract.isChildDocument(contentResolver, parent.uri, uri)

    // ====================
    // MediaUri
    // ====================

    fun mediaUri(context: Context): MediaUri? =
        MediaUri.fromDocumentUri(context, this)

    companion object {
        fun parse(uriString: String): DocumentUri =
            uriString.toUri().documentUri

        fun fromDocumentFile(documentFile: DocumentFile): DocumentUri =
            documentFile.uri.documentUri

        /**
         * Doesn't do anything for e.g. [treeUri]=_content://com.android.externalstorage.documents/tree/6164-3862%3ANavigated/document/6164-3862%3ANavigated%2FFilename_
         *
         * @see DocumentFile.fromTreeUri
         */
        fun fromTreeUri(context: Context, treeUri: Uri): DocumentUri? =
            DocumentFile.fromTreeUri(context, treeUri)?.let { fromDocumentFile(it) }

        fun fromMediaUri(context: Context, mediaUri: MediaUri): DocumentUri? =
            mediaUri.documentUri(context)

        /**
         * @see DocumentFile.fromFile
         */
        fun fromFile(file: File): DocumentUri =
            fromDocumentFile(DocumentFile.fromFile(file))
    }
}

val Uri.decoded: String
    get() = Uri.decode(toString())

val Uri.documentUri: DocumentUri
    get() = DocumentUri(this)

fun Uri.documentUriIfValid(context: Context): DocumentUri? =
    if (isDocumentUri(context)) {
        documentUri
    } else {
        null
    }

/**
 * @see DocumentFile.isDocumentUri
 */
fun Uri.isDocumentUri(context: Context): Boolean =
    DocumentFile.isDocumentUri(context, this)
