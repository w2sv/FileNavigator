package com.w2sv.common.utils

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.child
import com.anggrayudi.storage.file.getSimplePath

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