package com.w2sv.database.model

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
    val fileSourceKind: SourceType.Kind,
    val destinationDocumentUri: Uri,
    val movedFileDocumentUri: Uri,
    val movedFileMediaUri: Uri,
    @PrimaryKey val dateTime: LocalDateTime
) {
    constructor(moveEntry: MoveEntry) : this(
        fileName = moveEntry.fileName,
        fileType = moveEntry.fileTypeConfig,
        fileSourceKind = moveEntry.fileSourceKindConfig,
        destinationDocumentUri = moveEntry.destinationDocumentUri,
        movedFileDocumentUri = moveEntry.movedFileDocumentUri,
        movedFileMediaUri = moveEntry.movedFileMediaUri,
        dateTime = moveEntry.dateTime
    )

    fun asExternalModel(): MoveEntry =
        MoveEntry(
            fileName = fileName,
            fileTypeConfig = fileType,
            fileSourceKindConfig = fileSourceKind,
            destinationDocumentUri = destinationDocumentUri,
            movedFileDocumentUri = movedFileDocumentUri,
            movedFileMediaUri = movedFileMediaUri,
            dateTime = dateTime
        )
}