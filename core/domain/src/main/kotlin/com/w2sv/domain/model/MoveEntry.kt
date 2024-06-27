package com.w2sv.domain.model

import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri
import java.time.LocalDateTime

data class MoveEntry(
    val fileName: String,
    val fileType: FileType,
    val sourceType: SourceType,
    val destinationDocumentUri: DocumentUri,
    val movedFileDocumentUri: DocumentUri,
    val movedFileMediaUri: MediaUri,
    val dateTime: LocalDateTime,
    val autoMoved: Boolean
) {
    val fileAndSourceType by lazy {
        FileAndSourceType(fileType, sourceType)
    }
}