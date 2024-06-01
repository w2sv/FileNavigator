package com.w2sv.navigator.moving

import android.net.Uri
import com.w2sv.domain.model.MoveEntry
import java.time.LocalDateTime

internal fun getMoveEntry(
    moveFile: MoveFile,
    destinationDocumentUri: Uri,
    movedFileDocumentUri: Uri,
    movedFileMediaUri: Uri,
    dateTime: LocalDateTime
): MoveEntry =
    MoveEntry(
        fileName = moveFile.mediaStoreFile.columnData.name,
        fileType = moveFile.fileType,
        sourceType = moveFile.sourceType,
        destinationDocumentUri = destinationDocumentUri,
        movedFileDocumentUri = movedFileDocumentUri,
        movedFileMediaUri = movedFileMediaUri,
        dateTime = dateTime
    )