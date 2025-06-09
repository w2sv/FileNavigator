package com.w2sv.navigator.moving.model

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.MediaUri
import com.w2sv.common.util.log
import com.w2sv.domain.model.MovedFile
import java.time.LocalDateTime
import kotlinx.parcelize.Parcelize
import slimber.log.i

internal typealias AnyMoveBundle = MoveBundle<*, *>

/**
 * Bundle of all the data required for the move operation and all downstream actions.
 */
internal sealed interface MoveBundle<MD : MoveDestination, DSM : DestinationSelectionManner> :
    Parcelable {

    val file: MoveFile
    val destination: MD
    val destinationSelectionManner: DSM

    sealed interface Batchable<Mode : DestinationSelectionManner.NotificationBased> :
        MoveBundle<MoveDestination.Directory, Mode> {

        /**
         * Whether this MoveBundle is part of a batch move operation.
         */
        val batched: Boolean
    }

    @Parcelize
    data class FileDestinationPicked(
        override val file: MoveFile,
        override val destination: MoveDestination.File,
        override val destinationSelectionManner: DestinationSelectionManner.Picked
    ) : MoveBundle<MoveDestination.File, DestinationSelectionManner.Picked>

    @Parcelize
    data class DirectoryDestinationPicked(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val destinationSelectionManner: DestinationSelectionManner.Picked,
        override val batched: Boolean = true
    ) : Batchable<DestinationSelectionManner.Picked>

    @Parcelize
    data class QuickMove(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val destinationSelectionManner: DestinationSelectionManner.Quick,
        override val batched: Boolean
    ) : Batchable<DestinationSelectionManner.Quick>

    @Parcelize
    data class AutoMove(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val destinationSelectionManner: DestinationSelectionManner.Auto
    ) : MoveBundle<MoveDestination.Directory, DestinationSelectionManner.Auto>

    fun movedFile(context: Context, dateTime: LocalDateTime): MovedFile {
        val name = destination.fileName(context)
        val originalName = file.mediaStoreFileData.name.takeIf { it != name }
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
                val movedFileDocumentUri = destination.documentUri.constructChildDocumentUri(fileName = file.mediaStoreFileData.name)
                MovedFile.Local(
                    documentUri = movedFileDocumentUri,
                    mediaUri = capturedDestination.movedFileMediaUri(
                        movedFileDocumentUri = movedFileDocumentUri,
                        moveFile = file,
                        fileHasBeenAutoMoved = destinationSelectionManner.isAuto,
                        context = context
                    ),
                    name = file.mediaStoreFileData.name,
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
        const val EXTRA = "com.w2sv.navigator.extra.MoveBundle"

        inline fun <reified MB : AnyMoveBundle> fromIntent(intent: Intent): MB =
            intent.getParcelableCompat<MB>(EXTRA)!!
    }
}

private fun MoveDestination.Directory.movedFileMediaUri(
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
    val mediaUri =
        requireNotNull(
            moveFile.mediaUri.idIncremented()
        ) // Should never be null here, since the original mediaUri originates from the system and should therefore be correct
    val reconstructedDocumentUri = mediaUri.documentUri(context)
    return when {
        reconstructedDocumentUri == null -> {
            i { "Reconstructed document uri null" }
            null
        }

        reconstructedDocumentUri.fileName(context) == moveFile.mediaStoreFileData.name -> {
            i { "File name corresponding to the reconstructed document Uri matches the original one" }
            mediaUri
        }

        else -> {
            i {
                "File name of reconstructed document Uri=${reconstructedDocumentUri.fileName(
                    context
                )} does not match original file name=${moveFile.mediaStoreFileData.name}"
            }
            null
        }
    }
}
