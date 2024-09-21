package com.w2sv.navigator.moving.model

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.model.MoveEntry
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

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
         * Whether MoveBundle is part of a batch move operation.
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

    fun moveEntry(
        context: Context,
        dateTime: LocalDateTime,
    ): MoveEntry =
        when (val capturedDestination = destination) {
            is MoveDestination.File -> {
                MoveEntry(
                    fileName = capturedDestination.fileName(context),
                    fileType = file.fileType,
                    sourceType = file.sourceType,
                    destination = MoveDestination.Directory(capturedDestination.documentUri.parent!!),
                    movedFileDocumentUri = capturedDestination.documentUri,
                    movedFileMediaUri = capturedDestination.mediaUri,
                    dateTime = dateTime,
                    autoMoved = destinationSelectionManner.isAuto
                )
            }

            is MoveDestination.Directory -> {
                val movedFileDocumentUri =
                    destination.documentUri.childDocumentUri(fileName = file.mediaStoreFileData.name)
                MoveEntry(
                    fileName = file.mediaStoreFileData.name,
                    fileType = file.fileType,
                    sourceType = file.sourceType,
                    destination = capturedDestination,
                    movedFileDocumentUri = movedFileDocumentUri,
                    movedFileMediaUri = movedFileDocumentUri.mediaUri(context)!!,  // TODO
                    dateTime = dateTime,
                    autoMoved = destinationSelectionManner.isAuto
                )
            }

            else -> throw IllegalArgumentException()  // I have no clue why this is necessary here
        }


    companion object {
        const val EXTRA = "com.w2sv.navigator.extra.MoveBundle"

        fun <MD : MoveDestination, Mode : DestinationSelectionManner> fromIntent(intent: Intent): MoveBundle<MD, Mode> =
            intent.getParcelableCompat<MoveBundle<MD, Mode>>(EXTRA)!!
    }
}