package com.w2sv.navigator.moving

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri
import com.w2sv.core.domain.R
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveEntry
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.mediastore.MediaStoreFile
import kotlinx.parcelize.IgnoredOnParcel
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

    @IgnoredOnParcel
    val isGif: Boolean by lazy {
        fileType is FileType.Image && mediaStoreFile.columnData.fileExtension.lowercase() == "gif"
    }

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

    fun label(
        context: Context
    ): String =
        when {
            isGif -> context.getString(R.string.gif)
            else -> {
                when (sourceType) {
                    SourceType.Screenshot, SourceType.Recording -> context.getString(
                        sourceType.labelRes
                    )

                    SourceType.Camera -> context.getString(
                        when (fileType) {
                            FileType.Image -> R.string.photo
                            FileType.Video -> R.string.video
                            else -> throw Error()
                        }
                    )

                    SourceType.Download -> context.getString(
                        R.string.file_type_download,
                        context.getString(fileType.labelRes),
                    )

                    SourceType.OtherApp -> "/${mediaStoreFile.columnData.dirName} ${
                        context.getString(
                            fileType.labelRes
                        )
                    }"
                }
            }
        }

    companion object {
        const val EXTRA = "com.w2sv.filenavigator.extra.MoveFile"

        fun fromIntent(intent: Intent): MoveFile =
            intent.getParcelableCompat<MoveFile>(EXTRA)!!
    }
}