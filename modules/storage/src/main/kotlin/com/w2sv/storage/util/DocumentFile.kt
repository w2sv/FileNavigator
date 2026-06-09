package com.w2sv.storage.util

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.child

fun DocumentFile.hasChild(context: Context, path: String, requiresWriteAccess: Boolean = false): Boolean =
    child(context, path, requiresWriteAccess) != null
