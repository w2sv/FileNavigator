package com.w2sv.domain.model

import android.net.Uri
import com.w2sv.domain.model.navigatorconfig.FileType
import java.time.LocalDateTime

data class MoveEntry(
    val fileName: String,
    val fileType: FileType.Kind,
    val fileSourceKind: FileType.Source.Kind,
    val destinationDocumentUri: Uri,
    val movedFileDocumentUri: Uri,
    val movedFileMediaUri: Uri,
    val dateTime: LocalDateTime
)