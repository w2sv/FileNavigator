package com.w2sv.data.storage.database.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveEntry
import java.time.LocalDateTime

@Entity
data class MoveEntryEntity(
    val fileName: String,
    val fileType: FileType,
    val fileSourceKind: FileType.Source.Kind,
    val destinationDocumentUri: Uri,
    @PrimaryKey val dateTime: LocalDateTime
) {
    constructor(moveEntry: MoveEntry) : this(
        fileName = moveEntry.fileName,
        fileType = moveEntry.fileType,
        fileSourceKind = moveEntry.fileSourceKind,
        destinationDocumentUri = moveEntry.destinationDocumentUri,
        dateTime = moveEntry.dateTime
    )

    fun asExternalModel(): MoveEntry =
        MoveEntry(
            fileName = fileName,
            fileType = fileType,
            fileSourceKind = fileSourceKind,
            destinationDocumentUri = destinationDocumentUri,
            dateTime = dateTime
        )
}