package com.w2sv.navigator.moving.model

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.model.MoveEntry
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

/**
 * Bundle of all the data required for the move operation and all downstream actions.
 */
internal sealed interface MoveBundle<MD : MoveDestination, DSM : DestinationSelectionManner> : Parcelable {
    val file: MoveFile
    val destination: MD
    val selection: DSM

    sealed interface Batchable<Mode: DestinationSelectionManner.Batchable>: MoveBundle<MoveDestination.Directory, Mode>

    @Parcelize
    data class QuickMove(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val selection: DestinationSelectionManner.Quick
    ) : Batchable<DestinationSelectionManner.Quick>

    @Parcelize
    data class AutoMove(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val selection: DestinationSelectionManner.Auto
    ) : MoveBundle<MoveDestination.Directory, DestinationSelectionManner.Auto>

    @Parcelize
    data class DestinationPicked(
        override val file: MoveFile,
        override val destination: MoveDestination.File,
        override val selection: DestinationSelectionManner.Picked
    ) : MoveBundle<MoveDestination.File, DestinationSelectionManner.Picked>

    @Parcelize
    data class DestinationPickedBatchMove(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val selection: DestinationSelectionManner.Picked
    ) : Batchable<DestinationSelectionManner.Picked>

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
                    autoMoved = selection.isAuto
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
                    autoMoved = selection.isAuto
                )
            }

            else -> throw IllegalArgumentException()  // I have no clue why this is necessary here
        }


    companion object {
        const val EXTRA = "com.w2sv.navigator.extra.MoveBundle"

        // TODO: is it possible to make this a Parcelable extension function?
        fun <MD : MoveDestination, Mode : DestinationSelectionManner> fromIntent(intent: Intent): MoveBundle<MD, Mode> =
            intent.getParcelableCompat<MoveBundle<MD, Mode>>(EXTRA)!!
    }
}