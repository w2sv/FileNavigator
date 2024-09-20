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
internal sealed interface MoveBundle<Dest : MoveDestination, Mode : MoveMode> : Parcelable {
    val file: MoveFile
    val destination: Dest
    val mode: Mode

    sealed interface Batchable<Mode: MoveMode.Batchable>: MoveBundle<MoveDestination.Directory, Mode>

    @Parcelize
    data class QuickMove(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val mode: MoveMode.Quick
    ) : Batchable<MoveMode.Quick>

    @Parcelize
    data class AutoMove(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val mode: MoveMode.Auto
    ) : MoveBundle<MoveDestination.Directory, MoveMode.Auto>

    @Parcelize
    data class DestinationPicked(
        override val file: MoveFile,
        override val destination: MoveDestination.File,
        override val mode: MoveMode.Picked
    ) : MoveBundle<MoveDestination.File, MoveMode.Picked>

    @Parcelize
    data class DestinationPickedBatchMove(
        override val file: MoveFile,
        override val destination: MoveDestination.Directory,
        override val mode: MoveMode.Picked
    ) : Batchable<MoveMode.Picked>

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
                    autoMoved = mode.isAuto
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
                    autoMoved = mode.isAuto
                )
            }

            else -> throw IllegalArgumentException()  // I have no clue why this is necessary here
        }


    companion object {
        const val EXTRA = "com.w2sv.navigator.extra.MoveBundle"

        // TODO: is it possible to make this a Parcelable extension function?
        fun <Dest : MoveDestination, Mode : MoveMode> fromIntent(intent: Intent): MoveBundle<Dest, Mode> =
            intent.getParcelableCompat<MoveBundle<Dest, Mode>>(EXTRA)!!
    }
}