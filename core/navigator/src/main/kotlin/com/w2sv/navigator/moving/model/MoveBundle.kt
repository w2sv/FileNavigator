package com.w2sv.navigator.moving.model

import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri
import com.w2sv.domain.model.MoveEntry
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

/**
 * Bundle of all the data required for the actual move operation and all possible downstream actions.
 *
 * Consumed by MoveBroadcastReceiver.
 */
@Parcelize
internal data class MoveBundle(
    val file: MoveFile,
    val destination: DocumentUri,
    val mode: MoveMode
) : Parcelable {

    fun moveEntry(
        movedFileDocumentUri: DocumentUri,
        movedFileMediaUri: MediaUri,
        dateTime: LocalDateTime,
    ): MoveEntry =
        MoveEntry(
            fileName = file.mediaStoreData.name,
            fileType = file.fileType,
            sourceType = file.sourceType,
            destinationDocumentUri = destination,
            movedFileDocumentUri = movedFileDocumentUri,
            movedFileMediaUri = movedFileMediaUri,
            dateTime = dateTime,
            autoMoved = mode.isAuto
        )

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.MoveBundle"

        fun fromIntent(intent: Intent): MoveBundle =
            intent.getParcelableCompat<MoveBundle>(EXTRA)!!
    }
}