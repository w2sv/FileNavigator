package com.w2sv.filenavigator.navigator

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.anggrayudi.storage.media.MediaFile
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.androidutils.notifying.getNotificationManager
import com.w2sv.filenavigator.FileType
import com.w2sv.filenavigator.navigator.mediastore.MediaStoreFileData
import com.w2sv.filenavigator.navigator.service.FileNavigatorService
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * @param uri The MediaStore URI.
 */
@Parcelize
data class MoveFile(
    val uri: Uri,
    val type: FileType,
    val sourceKind: FileType.SourceKind,
    val data: MediaStoreFileData
) : Parcelable {

    @IgnoredOnParcel
    val defaultTargetDir: FileType.Source.DefaultTargetDir by lazy {
        FileType.Source.DefaultTargetDir(type.identifier, sourceKind)
    }

    fun getMediaFile(context: Context): MediaFile? =
        MediaStoreCompat.fromMediaId(
            context,
            type.simpleStorageType,
            data.id
        )

    @Parcelize
    data class NotificationParameters(
        val notificationId: Int,
        val requestCodes: ArrayList<Int>
    ) : Parcelable {

        companion object {
            const val EXTRA = "com.w2sv.filenavigator.extra.NOTIFICATION_PARAMETERS"
        }

        fun cancelUnderlyingNotification(context: Context){
            context.getNotificationManager().cancel(notificationId)

            FileNavigatorService.onNotificationCancelled(
                this,
                context
            )
        }
    }
}