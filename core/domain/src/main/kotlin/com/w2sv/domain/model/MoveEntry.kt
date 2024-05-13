package com.w2sv.domain.model

import android.net.Uri
import java.time.LocalDateTime

data class MoveEntry(
    val fileName: String,
    val fileType: FileType,
    val fileSourceKind: FileType.Source.Kind,
    val destinationDocumentUri: Uri,
    val movedFileMediaUri: Uri,
    val dateTime: LocalDateTime
) {
    val source: FileType.Source
        get() = FileType.Source(fileType, fileSourceKind)
}