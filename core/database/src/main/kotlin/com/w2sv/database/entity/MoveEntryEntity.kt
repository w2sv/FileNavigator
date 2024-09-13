package com.w2sv.database.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.documentUri
import com.w2sv.common.utils.mediaUri
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveDestination
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
        destinationDocumentUri = moveEntry.destination.documentUri.uri,
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
            destination = MoveDestination(DocumentUri(destinationDocumentUri)),
            movedFileDocumentUri = movedFileDocumentUri.documentUri,
            movedFileMediaUri = movedFileMediaUri.mediaUri,
            dateTime = dateTime,
            autoMoved = autoMoved
        )
}