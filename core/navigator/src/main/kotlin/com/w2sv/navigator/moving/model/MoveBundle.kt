package com.w2sv.navigator.moving.model

import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri
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
        movedFileDocumentUri: DocumentUri,
        movedFileMediaUri: MediaUri,
        dateTime: LocalDateTime,
    ): MoveEntry =
        MoveEntry(
            fileName = file.mediaStoreFileData.name,
            fileType = file.fileType,
            sourceType = file.sourceType,
            destination = destination,
            movedFileDocumentUri = movedFileDocumentUri,
            movedFileMediaUri = movedFileMediaUri,
            dateTime = dateTime,
            autoMoved = mode.isAuto
        )

    companion object {
        const val EXTRA = "com.w2sv.navigator.extra.MoveBundle"

        fun fromIntent(intent: Intent): MoveBundle =  // TODO: is it possible to make this a Parcelable extension function?
            intent.getParcelableCompat<MoveBundle>(EXTRA)!!
    }
}