package com.w2sv.navigator.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.common.utils.showToast
import com.w2sv.navigator.moving.MoveException
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import slimber.log.i
import java.io.File

internal class ViewFileIfPresentBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        i { "ViewFileIfPresentBroadcastReceiver.onReceive" }
        if (context == null || intent == null) return

        val mediaUri = intent.getParcelableCompat<Uri>(Extra.MEDIA_URI)
        val mimeType = intent.getStringExtra(Extra.MIME_TYPE)
        val absPath = intent.getStringExtra(Extra.ABS_PATH)!!

        if (File(absPath).exists()) {
            context.applicationContext.startActivity(
                Intent()
                    .setAction(Intent.ACTION_VIEW)
                    .setDataAndType(
                        mediaUri,
                        mimeType
                    )
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)  // Resolves "java.lang.RuntimeException: Unable to start receiver com.w2sv.navigator.notifications.ViewFileIfPresentBroadcastReceiver: android.util.AndroidRuntimeException: Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?"
            )
        } else {
            context.showToast(MoveException.MoveFileNotFound.toastProperties)
            NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
                context = context,
                intent = intent
            )
        }
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
        fun intent(
            context: Context,
            mediaUri: Uri,
            absPath: String,
            mimeType: String,
            notificationResources: NotificationResources
        ): Intent =
            Intent(context, ViewFileIfPresentBroadcastReceiver::class.java)
                .putExtra(Extra.MIME_TYPE, mimeType)
                .putExtra(Extra.MEDIA_URI, mediaUri)
                .putExtra(Extra.ABS_PATH, absPath)
                .putNotificationResourcesExtra(notificationResources)
    }
}