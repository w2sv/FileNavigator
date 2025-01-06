package com.w2sv.navigator.notifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.util.MediaUri
import com.w2sv.core.navigator.R
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.shared.putOptionalNotificationResourcesExtra
import java.io.File
import kotlinx.parcelize.Parcelize

internal class ViewFileIfPresentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(Args.fromIntent(intent)) {
            if (File(absPath).exists()) {
                startActivity(
                    Intent()
                        .setAction(Intent.ACTION_VIEW).setDataAndType(mediaUri.uri, mimeType)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            } else {
                showToast(R.string.file_has_already_been_moved_or_deleted)
                NotificationResources
                    .fromIntent(intent)
                    ?.cancelNotification(this@ViewFileIfPresentActivity)
            }
        }

        finishAndRemoveTask()
    }

    @Parcelize
    data class Args(val mediaUri: MediaUri, val mimeType: String, val absPath: String) :
        Parcelable {

        constructor(moveFile: MoveFile) : this(
            mediaUri = moveFile.mediaUri,
            mimeType = moveFile.fileType.simpleStorageMediaType.mimeType,
            absPath = moveFile.mediaStoreFileData.absPath
        )

        companion object {
            const val EXTRA = "com.w2sv.filenavigator.extra.ViewFileIfPresentActivity.Args"

            fun fromIntent(intent: Intent): Args =
                intent.getParcelableCompat<Args>(EXTRA)!!
        }
    }

    companion object {
        fun makeRestartActivityIntent(
            context: Context,
            args: Args,
            notificationResources: NotificationResources
        ): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    ViewFileIfPresentActivity::class.java
                )
            )
                .putExtra(Args.EXTRA, args)
                .putOptionalNotificationResourcesExtra(notificationResources)
    }
}
