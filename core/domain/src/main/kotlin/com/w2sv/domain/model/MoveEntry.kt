package com.w2sv.domain.model

import android.net.Uri
import java.time.LocalDateTime

data class MoveEntry(
    val fileName: String,
    val fileType: FileType,
    val sourceType: SourceType,
    val destinationDocumentUri: Uri,
    val movedFileDocumentUri: Uri,
    val movedFileMediaUri: Uri,
    val dateTime: LocalDateTime,
    val autoMoved: Boolean
) {
    val combinedFileAndSourceTypeIconRes: Int
        get() = combinedFileAndSourceTypeIconRes(fileType, sourceType)
}