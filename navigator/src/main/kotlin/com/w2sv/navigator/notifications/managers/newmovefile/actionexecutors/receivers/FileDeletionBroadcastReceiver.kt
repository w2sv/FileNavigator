package com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.navigator.R
import com.w2sv.navigator.model.NavigatableFile
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.newmovefile.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.putMoveFileExtra
import com.w2sv.navigator.notifications.putNotificationResourcesExtra
import slimber.log.e

class FileDeletionBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null)
            return

        // Attempt deletion
        val snackbarTextRes = attemptFileDeletion(context, intent)

        // Show result snackbar
        context.showToast(
            text = snackbarTextRes,
            duration = Toast.LENGTH_LONG
        )

        // Clean up notification resources
        NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
            context,
            intent
        )
    }

    /**
     * @return Result snackbar text resource id.
     */
    private fun attemptFileDeletion(context: Context, intent: Intent): Int =
        when {
            !isExternalStorageManger() -> R.string.file_deletion_requires_access_to_manage_all_files
            else -> {
                val navigatableFile =
                    intent.getParcelableCompat<NavigatableFile>(NavigatableFile.EXTRA)!!

                val mediaFile = navigatableFile.getSimpleStorageMediaFile(context)
                    .also {
                        if (it == null) {
                            e { "mediaFile=null" }
                        }
                    }

                if (mediaFile?.delete() == true) R.string.successfully_deleted_file else R.string.couldn_t_delete_file
            }
        }

    companion object {
        fun getIntent(
            navigatableFile: NavigatableFile,
            notificationResources: NotificationResources,
            context: Context
        ): Intent =
            Intent(context, FileDeletionBroadcastReceiver::class.java)
                .putMoveFileExtra(navigatableFile)
                .putNotificationResourcesExtra(notificationResources)
    }
}