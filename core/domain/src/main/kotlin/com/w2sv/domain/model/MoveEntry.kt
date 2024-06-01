package com.w2sv.domain.model

import android.net.Uri
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import java.time.LocalDateTime

data class MoveEntry(
    val fileName: String,
    val fileTypeConfig: FileType,
    val fileSourceKindConfig: SourceType,
    val destinationDocumentUri: Uri,
    val movedFileDocumentUri: Uri,
    val movedFileMediaUri: Uri,
    val dateTime: LocalDateTime
)