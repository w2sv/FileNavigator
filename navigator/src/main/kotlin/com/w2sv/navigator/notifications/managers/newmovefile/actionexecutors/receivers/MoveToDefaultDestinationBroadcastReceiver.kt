package com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.getSimplePath
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.R
import com.w2sv.navigator.model.NavigatableFile
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.newmovefile.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.putDefaultMoveDestinationExtra
import com.w2sv.navigator.notifications.putNavigatableFileExtra
import com.w2sv.navigator.notifications.putNotificationResourcesExtra
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoveToDefaultDestinationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (!isExternalStorageManger()) {
            context.showToast(
                "Moving to default destination requires permission to manage all files!",
                duration = Toast.LENGTH_LONG
            )
        }

        val navigatableFile =
            intent.getParcelableCompat<NavigatableFile>(NavigatableFile.EXTRA)!!
        val defaultMoveDestination =
            intent.getParcelableCompat<Uri>(FileNavigator.EXTRA_DEFAULT_MOVE_DESTINATION)!!

        // Exit on unsuccessful conversion to SimpleStorage objects
        val targetDirectoryDocumentFile =
            DocumentFile.fromSingleUri(context, defaultMoveDestination)
        val moveMediaFile = navigatableFile.getSimpleStorageMediaFile(context)

        if (targetDirectoryDocumentFile == null || moveMediaFile == null) {
            context.showToast(R.string.couldn_t_move_file)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            moveMediaFile.moveTo(
                targetDirectoryDocumentFile,
                callback = object : FileCallback() {
                    override fun onCompleted(result: Any) {
                        context.showToast(
                            context.getString(
                                R.string.moved_file_to,
                                targetDirectoryDocumentFile.getSimplePath(context)
                            )
                        )
                    }

                    // TODO: refined errorCode handling
                    override fun onFailed(errorCode: ErrorCode) {
                        context.showToast(R.string.couldn_t_move_file)
                    }
                }
            )
        }

        NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(context, intent)
    }

    companion object {
        fun getIntent(
            navigatableFile: NavigatableFile,
            notificationResources: NotificationResources,
            defaultMoveDestination: Uri,
            context: Context
        ): Intent =
            Intent(context, MoveToDefaultDestinationBroadcastReceiver::class.java)
                .putNavigatableFileExtra(navigatableFile)
                .putNotificationResourcesExtra(notificationResources)
                .putDefaultMoveDestinationExtra(defaultMoveDestination)
    }
}