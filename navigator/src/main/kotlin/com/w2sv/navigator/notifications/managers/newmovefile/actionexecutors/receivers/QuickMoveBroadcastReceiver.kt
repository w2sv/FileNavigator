package com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.getSimplePath
import com.anggrayudi.storage.media.MediaFile
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.navigator.R
import com.w2sv.navigator.model.NavigatableFile
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.newmovefile.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.putNavigatableFileExtra
import com.w2sv.navigator.notifications.putNotificationResourcesExtra
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import slimber.log.i

class QuickMoveBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        // Exit if 'manage all files' permission not granted
        if (!isExternalStorageManger()) {
            return context.showToast(
                context.getString(R.string.moving_to_default_destination_requires_permission_to_manage_all_files),
                duration = Toast.LENGTH_LONG
            )
        }

        // Extract extras
        val navigatableFile =
            intent.getParcelableCompat<NavigatableFile>(NavigatableFile.EXTRA)!!

        if (!navigatableFile.mediaStoreFile.columnData.fileExists) {
            removeNotificationAndCleanupResources(context, intent)
            context.showToast(R.string.couldn_t_move_file_has_already_been_moved_deleted_or_renamed)
            return
        }

        // Exit on unsuccessful conversion to SimpleStorage objects
        val targetDirectoryDocumentFile =
            DocumentFile.fromSingleUri(
                context,
                intent.getParcelableCompat<Uri>(EXTRA_MOVE_DESTINATION)!!
                    .also {
                        i { "Received move destination: $it" }
                    }
            ) ?: return context.showToast(R.string.internal_error)

        val moveMediaFile = navigatableFile.getSimpleStorageMediaFile(context)
            ?: return context.showToast(R.string.internal_error)

        moveMediaFile.launchMoveTo(
            targetFolder = targetDirectoryDocumentFile,
            callback = object : FileCallback() {
                override fun onCompleted(result: Any) {
                    context.showToast(
                        context.getString(
                            R.string.moved_file_to,
                            targetDirectoryDocumentFile.getSimplePath(context)
                        )
                    )
                    removeNotificationAndCleanupResources(context, intent)
                }

                override fun onFailed(errorCode: ErrorCode) {
                    i { errorCode.toString() }
                    context.showToast(errorCode.name)
                }
            }
        )
    }

    companion object {
        fun getIntent(
            navigatableFile: NavigatableFile,
            notificationResources: NotificationResources,
            moveDestination: Uri,
            context: Context
        ): Intent =
            Intent(context, QuickMoveBroadcastReceiver::class.java)
                .putNavigatableFileExtra(navigatableFile)
                .putNotificationResourcesExtra(notificationResources)
                .putExtra(
                    EXTRA_MOVE_DESTINATION,
                    moveDestination
                )

        private const val EXTRA_MOVE_DESTINATION =
            "com.w2sv.filenavigator.extra.MOVE_DESTINATION"
    }
}

fun removeNotificationAndCleanupResources(context: Context, intent: Intent) {
    NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
        context,
        intent
    )
}

fun MediaFile.launchMoveTo(targetFolder: DocumentFile, callback: FileCallback): Job =
    CoroutineScope(Dispatchers.IO)
        .launch {
            moveTo(
                targetFolder = targetFolder,
                callback = callback
            )
        }
