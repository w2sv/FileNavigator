package com.w2sv.navigator.notifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.common.utils.showToast
import com.w2sv.navigator.moving.MoveException
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import slimber.log.i
import java.io.File

internal class ViewFileIfPresentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mediaUri = intent.getParcelableCompat<Uri>(Extra.MEDIA_URI)
        val mimeType = intent.getStringExtra(Extra.MIME_TYPE)
        val absPath = intent.getStringExtra(Extra.ABS_PATH)!!

        if (File(absPath).exists()) {
            startActivity(
                Intent()
                    .setAction(Intent.ACTION_VIEW)
                    .setDataAndType(
                        mediaUri,
                        mimeType
                    )
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        } else {
            showToast(MoveException.MoveFileNotFound.toastProperties)
            NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
                context = this,
                intent = intent
            )
        }
        finishAndRemoveTask()
    }

    private data object Extra {
        const val MIME_TYPE =
            "com.w2sv.filenavigator.extra.ViewFileIfPresentBroadcastReceiver.MIME_TYPE"
        const val MEDIA_URI =
            "com.w2sv.filenavigator.extra.ViewFileIfPresentBroadcastReceiver.MEDIA_URI"
        const val ABS_PATH =
            "com.w2sv.filenavigator.extra.ViewFileIfPresentBroadcastReceiver.ABS_PATH"
    }

    companion object {
        fun makeRestartActivityIntent(
            context: Context,
            mediaUri: Uri,
            absPath: String,
            mimeType: String,
            notificationResources: NotificationResources
        ): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    ViewFileIfPresentActivity::class.java
                )
            )
                .putExtra(Extra.MIME_TYPE, mimeType)
                .putExtra(Extra.MEDIA_URI, mediaUri)
                .putExtra(Extra.ABS_PATH, absPath)
                .putNotificationResourcesExtra(notificationResources)
    }
}