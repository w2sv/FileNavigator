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
 * Bundle of all the data required for the move operation and all possible downstream actions.
 */
@Parcelize
internal data class MoveBundle(
    val file: MoveFile,
    val destination: MoveDestination,
    val mode: MoveMode
) : Parcelable {

    fun moveEntry(
        context: Context,
        dateTime: LocalDateTime,
    ): MoveEntry =
        when (destination) {
            is MoveDestination.File -> {
                MoveEntry(
                    fileName = file.mediaStoreFileData.name,  // TODO
                    fileType = file.fileType,
                    sourceType = file.sourceType,
                    destination = MoveDestination.Directory(destination.documentUri.parent!!),
                    movedFileDocumentUri = destination.documentUri,
                    movedFileMediaUri = destination.mediaUri,
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
                    destination = destination,
                    movedFileDocumentUri = movedFileDocumentUri,
                    movedFileMediaUri = movedFileDocumentUri.mediaUri(context)!!,  // TODO
                    dateTime = dateTime,
                    autoMoved = mode.isAuto
                )
            }
        }


    companion object {
        const val EXTRA = "com.w2sv.navigator.extra.MoveBundle"

        fun fromIntent(intent: Intent): MoveBundle =
            // TODO: is it possible to make this a Parcelable extension function?
            intent.getParcelableCompat<MoveBundle>(EXTRA)!!
    }
}