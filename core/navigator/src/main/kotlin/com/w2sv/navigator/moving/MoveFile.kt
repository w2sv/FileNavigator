package com.w2sv.navigator.moving

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveEntry
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.mediastore.MediaStoreFile
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
internal data class MoveFile(
    val mediaStoreFile: MediaStoreFile,
    val fileAndSourceType: FileAndSourceType,
    val moveMode: MoveMode?
) : Parcelable {

    fun simpleStorageMediaFile(context: Context): MediaFile? =
        MediaStoreCompat.fromMediaId(
            context,
            fileType.simpleStorageMediaType,
            mediaStoreFile.columnData.rowId
        )

    val fileType: FileType
        get() = fileAndSourceType.fileType

    val sourceType: SourceType
        get() = fileAndSourceType.sourceType

    fun moveEntry(
        destinationDocumentUri: DocumentUri,
        movedFileDocumentUri: DocumentUri,
        movedFileMediaUri: MediaUri,
        dateTime: LocalDateTime,
        autoMoved: Boolean
    ): MoveEntry =
        MoveEntry(
            fileName = mediaStoreFile.columnData.name,
            fileType = fileType,
            sourceType = sourceType,
            destinationDocumentUri = destinationDocumentUri,
            movedFileDocumentUri = movedFileDocumentUri,
            movedFileMediaUri = movedFileMediaUri,
            dateTime = dateTime,
            autoMoved = autoMoved
        )

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.MOVE_FILE"

        fun fromIntent(intent: Intent): MoveFile =
            intent.getParcelableCompat<MoveFile>(EXTRA)!!
    }
}