package com.w2sv.navigator.notifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.utils.MediaUri
import com.w2sv.common.utils.showToast
import com.w2sv.navigator.moving.MoveException
import kotlinx.parcelize.Parcelize
import java.io.File

internal class ViewFileIfPresentActivity : ComponentActivity() {

    @Parcelize
    data class Args(val mediaUri: MediaUri, val mimeType: String, val absPath: String) :
        Parcelable {

        companion object {
            const val EXTRA = "com.w2sv.filenavigator.extra.ViewFileIfPresentActivity.Args"

            fun fromIntent(intent: Intent): Args = intent.getParcelableCompat<Args>(EXTRA)!!
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = Args.fromIntent(intent)

        if (File(args.absPath).exists()) {
            startActivity(
                Intent()
                    .setAction(Intent.ACTION_VIEW).setDataAndType(args.mediaUri.uri, args.mimeType)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        } else {
            showToast(MoveException.MoveFileNotFound.toastProperties)
            NotificationResources.CleanupBroadcastReceiver.startFromResourcesComprisingIntent(
                context = this,
                intent = intent
            )
        }
        finishAndRemoveTask()
    }

    companion object {
        fun makeRestartActivityIntent(
            context: Context, args: Args, notificationResources: NotificationResources
        ): Intent = Intent.makeRestartActivityTask(
            ComponentName(
                context, ViewFileIfPresentActivity::class.java
            )
        ).putExtra(Args.EXTRA, args).putOptionalNotificationResourcesExtra(notificationResources)
    }
}