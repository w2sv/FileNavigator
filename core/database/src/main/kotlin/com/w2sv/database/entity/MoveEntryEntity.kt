package com.w2sv.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.w2sv.common.utils.documentUri
import com.w2sv.common.utils.mediaUri
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.model.MoveDestinationEntry
import com.w2sv.domain.model.MoveEntry
import com.w2sv.domain.model.SourceType
import java.time.LocalDateTime

@Entity
internal data class MoveEntryEntity(
    val fileName: String,
    val fileType: FileType,
    val sourceType: SourceType,
    @Embedded(prefix = "local_") val localDestinationEntryEntity: MoveDestinationEntryEntity.Local?,
    @Embedded(prefix = "external_") val externalDestinationEntryEntity: MoveDestinationEntryEntity.External?,
    @PrimaryKey val dateTime: LocalDateTime,
    val autoMoved: Boolean
) {
    constructor(moveEntry: MoveEntry) : this(
        fileName = moveEntry.fileName,
        fileType = moveEntry.fileType,
        sourceType = moveEntry.sourceType,
        localDestinationEntryEntity = moveEntry.destinationEntry.localOrNull?.let {
            MoveDestinationEntryEntity.Local(
                destination = it.destination.documentUri.uri,
                movedFileDocumentUri = it.movedFileDocumentUri.uri,
                movedFileMediaUri = it.movedFileMediaUri.uri
            )
        },
        externalDestinationEntryEntity = moveEntry.destinationEntry.externalOrNull?.let {
            MoveDestinationEntryEntity.External(
                destination = it.destination.documentUri.uri,
                providerPackageName = it.destination.providerPackageName,
                providerAppLabel = it.destination.providerAppLabel
            )
        },
        dateTime = moveEntry.dateTime,
        autoMoved = moveEntry.autoMoved
    )

    fun asExternal(): MoveEntry =
        MoveEntry(
            fileName = fileName,
            fileType = fileType,
            sourceType = sourceType,
            destinationEntry = requireNotNull(
                localDestinationEntryEntity
                    ?.let {
                        MoveDestinationEntry.Local(
                            destination = MoveDestination.Directory(it.destination.documentUri),
                            movedFileDocumentUri = it.movedFileDocumentUri.documentUri,
                            movedFileMediaUri = it.movedFileMediaUri.mediaUri
                        )
                    } ?: externalDestinationEntryEntity
                    ?.let {
                        MoveDestinationEntry.External(
                            MoveDestination.File.External(
                                documentUri = it.destination.documentUri,
                                providerPackageName = it.providerPackageName,
                                providerAppLabel = it.providerAppLabel
                            )
                        )
                    }
            ),
            dateTime = dateTime,
            autoMoved = autoMoved
        )
}