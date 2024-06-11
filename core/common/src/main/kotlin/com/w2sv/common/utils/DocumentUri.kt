package com.w2sv.common.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.child
import com.anggrayudi.storage.file.getSimplePath

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

fun DocumentFile.hasChild(
    context: Context,
    path: String,
    requiresWriteAccess: Boolean = false
): Boolean =
    child(context, path, requiresWriteAccess) != null

// TODO: write test
fun DocumentFile.fileName(context: Context): String =
    getSimplePath(context).let { simplePath ->
        val substringAfterLastSlash = simplePath.substringAfterLast("/")
        if (substringAfterLastSlash != simplePath) {
            substringAfterLastSlash
        } else {
            simplePath.substringAfterLast(":")
        }
    }