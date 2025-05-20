package com.w2sv.navigator.notifications.appnotifications.movefile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.coroutineScope
import com.w2sv.androidutils.res.getHtmlFormattedText
import com.w2sv.androidutils.widget.showToast
import com.w2sv.core.common.R
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.shared.DialogHostingActivity
import com.w2sv.navigator.shared.plus
import com.w2sv.navigator.shared.roundedCornersAlertDialogBuilder
import com.w2sv.navigator.shared.setIconHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Gets invoked when the user clicks the 'Delete file' notification action on the 'Move file' notification.
 * Must receive [MoveFile] & [NotificationResources] through its intent.
 *
 * This [DialogHostingActivity] shows a file deletion confirmation dialog. If the user confirms the deletion,
 * it attempts to carry out the deletion, shows a toast informing about the result, cancels the 'Move file' notification
 * corresponding to the file if it was successful and finishes. If the user declines the deletion or cancels the dialog by
 * clicking outside of its area, it finishes right away.
 */
internal class FileDeletionActivity : DialogHostingActivity() {

    private val moveFile by threadUnsafeLazy {
        MoveFile.fromIntent(intent)
    }

    private val notificationResources by threadUnsafeLazy {
        NotificationResources.Companion.fromIntent(intent)
    }

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
                        moveFile.mediaStoreFileData.name
                    )
                )
                .setPositiveButton(R.string.yes) { _, _ ->
                    launchFileDeletion()
                }
                .setNegativeButton(R.string.no) { _, _ ->
                    finishAndRemoveTask()
                }
                .setOnCancelListener {
                    finishAndRemoveTask()
                }
        )
    }

    private fun launchFileDeletion() {
        lifecycle.coroutineScope.launch {
            val successfullyDeleted = with(Dispatchers.IO) {
                contentResolver.delete(moveFile.mediaUri.uri, null) > 0
            }
            with(Dispatchers.Main) {
                if (successfullyDeleted) {
                    showToast(
                        resources.getHtmlFormattedText(
                            R.string.successfully_deleted,
                            moveFile.mediaStoreFileData.name
                        )
                    )
                    notificationResources.cancelNotification(this@FileDeletionActivity)
                } else {
                    showToast(
                        resources.getHtmlFormattedText(
                            R.string.couldn_t_delete,
                            moveFile.mediaStoreFileData.name
                        )
                    )
                }

                finishAndRemoveTask()
            }
        }
    }

    companion object {
        fun getIntent(
            moveFile: MoveFile,
            notificationResources: NotificationResources,
            context: Context
        ): Intent =
            Intent(context, FileDeletionActivity::class.java) + moveFile + notificationResources
    }
}
