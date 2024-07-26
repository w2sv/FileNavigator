package com.w2sv.navigator.moving.model

import android.content.Context
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.common.utils.MediaUri
import com.w2sv.core.domain.R
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.navigator.observing.model.MediaStoreData
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MoveFile(
    val mediaUri: MediaUri,
    val mediaStoreData: MediaStoreData,
    val fileAndSourceType: FileAndSourceType
) : Parcelable {

    fun simpleStorageMediaFile(context: Context): MediaFile? =
        MediaStoreCompat.fromMediaId(
            context = context,
            mediaType = fileType.simpleStorageMediaType,
            id = mediaStoreData.rowId
        )

    val fileType: FileType
        get() = fileAndSourceType.fileType

    val sourceType: SourceType
        get() = fileAndSourceType.sourceType

    @IgnoredOnParcel
    val isGif: Boolean by lazy {
        fileType is FileType.Image && mediaStoreData.extension.lowercase() == "gif"
    }

    fun moveNotificationLabel(
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

                    SourceType.OtherApp -> "/${mediaStoreData.containingDirName} ${
                        context.getString(
                            fileType.labelRes
                        )
                    }"
                }
            }
        }
}