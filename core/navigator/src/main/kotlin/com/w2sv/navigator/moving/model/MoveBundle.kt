package com.w2sv.navigator.moving.model

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.domain.model.MovedFile
import java.time.LocalDateTime
import kotlinx.parcelize.Parcelize

internal typealias AnyMoveBundle = MoveBundle<*, *>

/**
 * Bundle of all the data required for the move operation and all downstream actions.
 */
internal sealed interface MoveBundle<MD : NavigatorMoveDestination, DSM : DestinationSelectionManner> :
    Parcelable {

    val file: MoveFile
    val destination: MD
    val destinationSelectionManner: DSM

    sealed interface Batchable<Mode : DestinationSelectionManner.NotificationBased> :
        MoveBundle<NavigatorMoveDestination.Directory, Mode> {

        /**
         * Whether MoveBundle is part of a batch move operation.
         */
        val batched: Boolean
    }

    @Parcelize
    data class FileDestinationPicked(
        override val file: MoveFile,
        override val destination: NavigatorMoveDestination.File,
        override val destinationSelectionManner: DestinationSelectionManner.Picked
    ) : MoveBundle<NavigatorMoveDestination.File, DestinationSelectionManner.Picked>

    @Parcelize
    data class DirectoryDestinationPicked(
        override val file: MoveFile,
        override val destination: NavigatorMoveDestination.Directory,
        override val destinationSelectionManner: DestinationSelectionManner.Picked,
        override val batched: Boolean = true
    ) : Batchable<DestinationSelectionManner.Picked>

    @Parcelize
    data class QuickMove(
        override val file: MoveFile,
        override val destination: NavigatorMoveDestination.Directory,
        override val destinationSelectionManner: DestinationSelectionManner.Quick,
        override val batched: Boolean
    ) : Batchable<DestinationSelectionManner.Quick>

    @Parcelize
    data class AutoMove(
        override val file: MoveFile,
        override val destination: NavigatorMoveDestination.Directory,
        override val destinationSelectionManner: DestinationSelectionManner.Auto
    ) : MoveBundle<NavigatorMoveDestination.Directory, DestinationSelectionManner.Auto>

    fun movedFileEntry(context: Context, dateTime: LocalDateTime): MovedFile {
        val name = destination.fileName(context)
        val originalName = if (name != file.mediaStoreFileData.name) file.mediaStoreFileData.name else null
        return when (val capturedDestination = destination) {
            is NavigatorMoveDestination.File.Local -> {
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

            is NavigatorMoveDestination.File.External -> {
                MovedFile.External(
                    moveDestination = capturedDestination,
                    name = name,
                    originalName = originalName,
                    fileType = file.fileType,
                    sourceType = file.sourceType,
                    moveDateTime = dateTime
                )
            }

            is NavigatorMoveDestination.Directory -> {
                val movedFileDocumentUri = destination.documentUri.childDocumentUri(fileName = file.mediaStoreFileData.name)
                MovedFile.Local(
                    documentUri = movedFileDocumentUri,
                    mediaUri = movedFileDocumentUri.mediaUri(context)!!,
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
    }

    companion object {
        const val EXTRA = "com.w2sv.navigator.extra.MoveBundle"

        inline fun <reified MB : AnyMoveBundle> fromIntent(intent: Intent): MB =
            intent.getParcelableCompat<MB>(EXTRA)!!
    }
}
