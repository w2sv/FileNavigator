package com.w2sv.domain.model

import android.net.Uri
import com.w2sv.filesystem.DocumentUri
import java.time.LocalDateTime

data class MoveEntry(
    val fileName: String,
    val fileType: FileType,
    val sourceType: SourceType,
    val destination: DocumentUri,
    val movedFileUri: DocumentUri,
    val movedFileMediaUri: Uri,
    val dateTime: LocalDateTime,
    val autoMoved: Boolean
) {
    val combinedFileAndSourceTypeIconRes: Int
        get() = combinedFileAndSourceTypeIconRes(fileType, sourceType)
}