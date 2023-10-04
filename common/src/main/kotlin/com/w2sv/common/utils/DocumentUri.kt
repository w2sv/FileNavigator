package com.w2sv.common.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath

fun getDocumentUriPath(documentUri: Uri, context: Context): String? =
    DocumentFile.fromSingleUri(context, documentUri)?.getSimplePath(context)