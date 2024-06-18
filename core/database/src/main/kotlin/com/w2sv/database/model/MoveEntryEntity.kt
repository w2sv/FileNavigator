package com.w2sv.database.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveEntry
import com.w2sv.domain.model.SourceType
import java.time.LocalDateTime

@Entity
data class MoveEntryEntity(
    val fileName: String,
    val fileType: FileType,
    val sourceType: SourceType,
    val destinationDocumentUri: Uri,
    val movedFileDocumentUri: Uri,
    val movedFileMediaUri: Uri,
    @PrimaryKey val dateTime: LocalDateTime,
    val autoMoved: Boolean
) {
    constructor(moveEntry: MoveEntry) : this(
        fileName = moveEntry.fileName,
        fileType = moveEntry.fileType,
        sourceType = moveEntry.sourceType,
        destinationDocumentUri = moveEntry.destinationDocumentUri.uri,
        movedFileDocumentUri = moveEntry.movedFileDocumentUri.uri,
        movedFileMediaUri = moveEntry.movedFileMediaUri.uri,
        dateTime = moveEntry.dateTime,
        autoMoved = moveEntry.autoMoved
    )

    fun asExternalModel(): MoveEntry =
        MoveEntry(
            fileName = fileName,
            fileType = fileType,
            sourceType = sourceType,
            destinationDocumentUri = DocumentUri(destinationDocumentUri),
            movedFileDocumentUri = DocumentUri(movedFileDocumentUri),
            movedFileMediaUri = MediaUri(movedFileMediaUri),
            dateTime = dateTime,
            autoMoved = autoMoved
        )
}