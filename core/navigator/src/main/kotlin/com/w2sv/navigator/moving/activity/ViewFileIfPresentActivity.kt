package com.w2sv.navigator.moving.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.logging.LoggingComponentActivity
import com.w2sv.common.uri.MediaUri
import com.w2sv.common.util.restartActivityTaskIntent
import com.w2sv.core.common.R
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.domain.moving.MoveFileNotificationData
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
internal class ViewFileIfPresentActivity : LoggingComponentActivity() {

    @Inject
    lateinit var notificationEventHandler: NotificationEventHandler

    private val args by threadUnsafeLazy { MoveFileNotificationData(intent) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (File(args.absPath).exists()) {
            launchFileViewing()
        } else {
            onFileNotFound()
        }

        finishAndRemoveTask()
    }

    private fun launchFileViewing() {
        startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(args.mediaUri.uri, args.mimeType)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun onFileNotFound() {
        showToast(R.string.file_has_already_been_moved_or_deleted)
        notificationEventHandler(args.cancelNotificationEvent)
    }

    companion object {
        fun intent(args: MoveFileNotificationData, context: Context): Intent =
            restartActivityTaskIntent<ViewFileIfPresentActivity>(context)
                .putExtra(MoveFileNotificationData.EXTRA, args)
    }
}

private val MoveFileNotificationData.mediaUri: MediaUri
    get() = moveFile.mediaUri

private val MoveFileNotificationData.mimeType: String
    get() = moveFile.fileType.mediaType.mimeType

private val MoveFileNotificationData.absPath: String
    get() = moveFile.mediaStoreData.absPath
