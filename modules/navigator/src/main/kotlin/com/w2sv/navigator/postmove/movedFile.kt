package com.w2sv.navigator.postmove

import android.content.Context
import com.w2sv.core.logging.log
import com.w2sv.domain.model.MovedFile
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.NavigatableFile
import com.w2sv.storage.uri.DocumentUri
import com.w2sv.storage.uri.MediaUri
import java.time.LocalDateTime
import slimber.log.i

internal fun MoveOperation.movedFile(context: Context, dateTime: LocalDateTime): MovedFile {
    val name = destination.fileName(context)
    val originalName = file.mediaStoreEntry.fileName.takeIf { it != name }
    return when (val capturedDestination = destination) {
        is MoveDestination.File.Local -> {
            MovedFile.Local(
                documentUri = capturedDestination.documentUri,
                mediaUri = capturedDestination.mediaUri,
                name = name,
                originalName = originalName,
                fileType = file.fileType,
                sourceType = file.sourceType,
                moveDestination = capturedDestination.parent,
                moveDateTime = dateTime,
                autoMoved = destinationSelectionManner.isAuto
            )
        }

        is MoveDestination.File.External -> {
            MovedFile.External(
                moveDestination = capturedDestination,
                name = name,
                originalName = originalName,
                fileType = file.fileType,
                sourceType = file.sourceType,
                moveDateTime = dateTime
            )
        }

        is MoveDestination.Directory -> {
            val movedFileDocumentUri = destination.documentUri.constructChildDocumentUri(fileName = file.mediaStoreEntry.fileName)
            MovedFile.Local(
                documentUri = movedFileDocumentUri,
                mediaUri = movedFileMediaUri(
                    movedFileDocumentUri = movedFileDocumentUri,
                    navigatableFile = file,
                    fileHasBeenAutoMoved = destinationSelectionManner.isAuto,
                    context = context
                ),
                name = file.mediaStoreEntry.fileName,
                originalName = null,
                fileType = file.fileType,
                sourceType = file.sourceType,
                moveDestination = capturedDestination,
                moveDateTime = dateTime,
                autoMoved = destinationSelectionManner.isAuto
            )
        }
    }
        .log { "Created MovedFile.$it" }
}

private fun movedFileMediaUri(
    movedFileDocumentUri: DocumentUri,
    navigatableFile: NavigatableFile,
    fileHasBeenAutoMoved: Boolean,
    context: Context
): MediaUri? {
    movedFileDocumentUri.mediaUri(context)?.let { return it }

    if (!(movedFileDocumentUri.storageType.isSdCard && fileHasBeenAutoMoved)) {
        return null
    }

    i {
        "File has been auto moved to a SD Card destination;" +
            "Attempting to get the media Uri by incrementing the media Id of the original file"
    }

    // Should never be null here, since the original mediaUri originates from the system and should therefore be correct
    val mediaUri = checkNotNull(navigatableFile.mediaUri.idIncremented())
    val reconstructedDocumentUri = mediaUri.documentUri(context)
    return when {
        reconstructedDocumentUri == null -> {
            i { "Reconstructed document uri null" }
            null
        }

        reconstructedDocumentUri.fileName(context) == navigatableFile.mediaStoreEntry.fileName -> {
            i { "File name corresponding to the reconstructed document Uri matches the original one" }
            mediaUri
        }

        else -> {
            i {
                "File name of reconstructed document Uri=${
                    reconstructedDocumentUri.fileName(
                        context
                    )
                } does not match original file name=${navigatableFile.mediaStoreEntry.fileName}"
            }
            null
        }
    }
}
