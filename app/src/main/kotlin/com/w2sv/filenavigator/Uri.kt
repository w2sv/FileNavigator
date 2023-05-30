package com.w2sv.filenavigator

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import okio.Path.Companion.toPath
import java.io.File
import java.nio.file.Path

@JvmInline
value class DocumentUri(val value: Uri) {
    val documentId: String get() = DocumentsContract.getDocumentId(value)

    val treeDocumentUri: Uri
        get() = DocumentsContract.buildTreeDocumentUri(
            value.authority,
            documentId
        )

    fun getDocumentPath(
        contentResolver: ContentResolver,
        treeDocumentUri: Uri?
    ): DocumentsContract.Path? {
        val treeUri = treeDocumentUri ?: this.treeDocumentUri

        return DocumentsContract.findDocumentPath(
            contentResolver,
            DocumentsContract.buildChildDocumentsUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )
        )
    }

    fun getParentDocumentUri(): DocumentUri =
        DocumentUri(
            DocumentsContract.buildDocumentUriUsingTree(
                treeDocumentUri,
                DocumentsContract.getTreeDocumentId(treeDocumentUri)
            )
        )

    fun getDisplayName(contentResolver: ContentResolver): String? {
        try {
            contentResolver.query(
                value,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null
            )
                .use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val displayNameIndex = cursor.getColumnIndex(
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME
                        )
                        if (displayNameIndex != -1) {
                            val displayName = cursor.getString(displayNameIndex)
                            if (!displayName.isNullOrEmpty()) {
                                return displayName
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        fun buildFromTree(treeUri: Uri): DocumentUri =
            DocumentUri(
                DocumentsContract.buildDocumentUriUsingTree(
                    treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri)
                )
            )

        fun fromMediaUri(mediaUri: Uri, context: Context): DocumentUri =
            DocumentUri(MediaStore.getDocumentUri(context, mediaUri)!!)
    }
}

val DocumentsContract.Path.toOkioPath: okio.Path get() = path.joinToString(File.separator).toPath()
val DocumentsContract.Path.toNIOPath: Path get() = File(path.joinToString(File.separator)).toPath()

const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
