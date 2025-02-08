package com.w2sv.navigator.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.coroutineScope
import com.w2sv.androidutils.res.getHtmlFormattedText
import com.w2sv.androidutils.widget.showToast
import com.w2sv.core.navigator.R
import com.w2sv.navigator.moving.model.MoveFileWithNotificationResources
import com.w2sv.navigator.shared.DialogHostingActivity
import com.w2sv.navigator.shared.roundedCornersAlertDialogBuilder
import com.w2sv.navigator.shared.setIconHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import slimber.log.i

internal class FileDeletionActivity : DialogHostingActivity() {

    private val moveFileWithNotificationResources by lazy {
        MoveFileWithNotificationResources.fromIntent(intent)
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        i { "onCreate" }

        showFileDeletionConfirmationDialog()
    }

    private fun showFileDeletionConfirmationDialog() {
        showDialog(
            roundedCornersAlertDialogBuilder(this)
                .setIconHeader(R.drawable.ic_warning_24)
                .setMessage(
                    resources.getHtmlFormattedText(
                        R.string.file_deletion_confirmation_dialog_message,
                        moveFileWithNotificationResources.moveFile.mediaStoreFileData.name
                    )
                )
                .setPositiveButton(R.string.yes) { _, _ ->
                    launchFileDeletion()
                    dialog?.dismiss()
                }
                .setNegativeButton(R.string.no) { _, _ ->
                    finishAndRemoveTask()
                }
        )
    }

    private fun launchFileDeletion() {
        lifecycle.coroutineScope.launch {
            val successfullyDeleted = with(Dispatchers.IO) {
                contentResolver.delete(moveFileWithNotificationResources.moveFile.mediaUri.uri, null) > 0
            }
            with(Dispatchers.Main) {
                showToast(if (successfullyDeleted) "Successfully deleted" else "Couldn't delete")
                moveFileWithNotificationResources.notificationResources.cancelNotification(this@FileDeletionActivity)

                finishAndRemoveTask()
            }
        }
    }

    companion object {
        fun getIntent(moveFileWithNotificationResources: MoveFileWithNotificationResources, context: Context): Intent =
            Intent(context, FileDeletionActivity::class.java)
                .apply { moveFileWithNotificationResources.addToIntent(this) }
    }
}
