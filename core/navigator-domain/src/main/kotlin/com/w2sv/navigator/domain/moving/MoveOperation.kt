package com.w2sv.navigator.domain.moving

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.logging.log
import com.w2sv.common.uri.DocumentUri
import com.w2sv.common.uri.MediaUri
import com.w2sv.domain.model.MovedFile
import java.time.LocalDateTime
import kotlinx.parcelize.Parcelize
import slimber.log.i

/**
 * Bundle of all the data required for the move operation and all downstream actions.
 */
sealed interface MoveOperation : Parcelable {

    val file: MoveFile
    val destination: MoveDestination
    val destinationSelectionManner: DestinationSelectionManner
    val isPartOfBatch: Boolean get() = false

    sealed interface Batchable : MoveOperation {
        override val destination: MoveDestination.Directory
    }

    @Parcelize
    data class FileDestinationPicked(
        override val file: MoveFile,
        override val destination: MoveDestination.File,
        override val destinationSelectionManner: DestinationSelectionManner.Picked
    ) : MoveOperation

    @Parcelize
    data class DirectoryDestinationPicked(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val destinationSelectionManner: DestinationSelectionManner.Picked,
        override val isPartOfBatch: Boolean = true
    ) : Batchable

    @Parcelize
    data class QuickMove(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val destinationSelectionManner: DestinationSelectionManner.Quick,
        override val isPartOfBatch: Boolean
    ) : Batchable

    @Parcelize
    data class AutoMove(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val destinationSelectionManner: DestinationSelectionManner.Auto
    ) : MoveOperation

    fun movedFile(context: Context, dateTime: LocalDateTime): MovedFile {
        val name = destination.fileName(context)
        val originalName = file.mediaStoreData.name.takeIf { it != name }
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
                val movedFileDocumentUri = destination.documentUri.constructChildDocumentUri(fileName = file.mediaStoreData.name)
                MovedFile.Local(
                    documentUri = movedFileDocumentUri,
                    mediaUri = movedFileMediaUri(
                        movedFileDocumentUri = movedFileDocumentUri,
                        moveFile = file,
                        fileHasBeenAutoMoved = destinationSelectionManner.isAuto,
                        context = context
                    ),
                    name = file.mediaStoreData.name,
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

    companion object {
        const val EXTRA = "com.w2sv.navigator.extra.MoveOperation"

        inline operator fun <reified MB : MoveOperation> invoke(intent: Intent): MB =
            checkNotNull(intent.getParcelableCompat<MB>(EXTRA))
    }
}

private fun movedFileMediaUri(
    movedFileDocumentUri: DocumentUri,
    moveFile: MoveFile,
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
    val mediaUri = checkNotNull(moveFile.mediaUri.idIncremented())
    val reconstructedDocumentUri = mediaUri.documentUri(context)
    return when {
        reconstructedDocumentUri == null -> {
            i { "Reconstructed document uri null" }
            null
        }

        reconstructedDocumentUri.fileName(context) == moveFile.mediaStoreData.name -> {
            i { "File name corresponding to the reconstructed document Uri matches the original one" }
            mediaUri
        }

        else -> {
            i {
                "File name of reconstructed document Uri=${
                    reconstructedDocumentUri.fileName(
                        context
                    )
                } does not match original file name=${moveFile.mediaStoreData.name}"
            }
            null
        }
    }
}
