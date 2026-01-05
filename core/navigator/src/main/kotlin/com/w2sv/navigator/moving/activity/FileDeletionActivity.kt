package com.w2sv.navigator.moving.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.coroutineScope
import com.w2sv.androidutils.res.getHtmlFormattedText
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.util.intent
import com.w2sv.core.common.R
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.domain.moving.MoveFileNotificationData
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import com.w2sv.navigator.shared.roundedCornersAlertDialogBuilder
import com.w2sv.navigator.shared.setIconHeader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Gets invoked when the user clicks the 'Delete file' notification action on the 'Move file' notification.
 * Must receive [com.w2sv.navigator.domain.moving.MoveFile] & [com.w2sv.navigator.domain.notifications.CancelNotificationEvent] through its intent.
 *
 * Shows a file deletion confirmation dialog. If the user confirms the deletion,
 * it attempts to carry out the deletion, shows a toast informing about the result, cancels the 'Move file' notification
 * corresponding to the file if it was successful and finishes. If the user declines the deletion or cancels the dialog by
 * clicking outside of its area, it finishes right away.
 */
@AndroidEntryPoint
internal class FileDeletionActivity : DialogHostingActivity() {

    @Inject
    lateinit var notificationEventHandler: NotificationEventHandler

    private val args by threadUnsafeLazy { MoveFileNotificationData(intent) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showFileDeletionConfirmationDialog()
    }

    private fun showFileDeletionConfirmationDialog() {
        showDialog(
            roundedCornersAlertDialogBuilder(this)
                .setIconHeader(R.drawable.ic_warning_24)
                .setMessage(
                    resources.getHtmlFormattedText(
                        R.string.file_deletion_confirmation_dialog_message,
                        args.moveFile.mediaStoreData.name
                    )
                )
                .setPositiveButton(R.string.yes) { _, _ -> launchFileDeletion() }
                .setNegativeButton(R.string.no) { _, _ -> finishAndRemoveTask() }
                .setOnCancelListener { finishAndRemoveTask() }
        )
    }

    private fun launchFileDeletion() {
        lifecycle.coroutineScope.launch {
            val successfullyDeleted = with(Dispatchers.IO) {
                contentResolver.delete(args.moveFile.mediaUri.uri, null) > 0
            }
            with(Dispatchers.Main) {
                if (successfullyDeleted) {
                    showToast(
                        resources.getHtmlFormattedText(
                            R.string.successfully_deleted,
                            args.moveFile.mediaStoreData.name
                        )
                    )
                    notificationEventHandler(args.cancelNotificationEvent)
                } else {
                    showToast(
                        resources.getHtmlFormattedText(
                            R.string.couldn_t_delete,
                            args.moveFile.mediaStoreData.name
                        )
                    )
                }

                finishAndRemoveTask()
            }
        }
    }

    companion object {
        fun intent(args: MoveFileNotificationData, context: Context): Intent =
            intent<FileDeletionActivity>(context)
                .putExtra(MoveFileNotificationData.EXTRA, args)
    }
}
