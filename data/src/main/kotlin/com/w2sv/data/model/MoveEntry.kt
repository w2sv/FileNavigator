package com.w2sv.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class MoveEntry(
    val fileName: String,
    val originalLocation: String,
    val fileType: FileType,
    val fileSourceKind: FileType.Source.Kind,
    val destinationDocumentUri: Uri,
    @PrimaryKey val dateTime: LocalDateTime
) {
    val source: FileType.Source
        get() = FileType.Source(fileType, fileSourceKind)
}