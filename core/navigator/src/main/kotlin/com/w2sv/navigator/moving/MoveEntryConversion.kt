package com.w2sv.navigator.moving

import android.net.Uri
import com.w2sv.domain.model.MoveEntry
import java.time.LocalDateTime

internal fun MoveFile.getMoveEntry(
    destinationDocumentUri: Uri,
    movedFileDocumentUri: Uri,
    movedFileMediaUri: Uri,
    dateTime: LocalDateTime,
    autoMoved: Boolean
): MoveEntry =
    MoveEntry(
        fileName = mediaStoreFile.columnData.name,
        fileType = fileType,
        sourceType = sourceType,
        destinationDocumentUri = destinationDocumentUri,
        movedFileDocumentUri = movedFileDocumentUri,
        movedFileMediaUri = movedFileMediaUri,
        dateTime = dateTime,
        autoMoved = autoMoved
    )