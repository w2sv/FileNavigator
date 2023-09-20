package com.w2sv.navigator.actions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.R
import com.w2sv.navigator.model.MoveFile
import com.w2sv.navigator.notifications.AppNotificationsManager
import com.w2sv.navigator.notifications.NotificationResources
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.e
import javax.inject.Inject

@AndroidEntryPoint
class FileDeletionBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appNotificationsManager: AppNotificationsManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (!isExternalStorageManger()) {
            context.showToast(
                context.getString(R.string.file_deletion_requires_access_to_manage_all_files),
                duration = Toast.LENGTH_LONG
            )
            return
        }

        val moveFile =
            intent.getParcelableCompat<MoveFile>(FileNavigator.EXTRA_MOVE_FILE)!!
//        val file = File(moveFile.data.absPath).also { i{"File exists: ${it.exists()}"} }
//        val deleted = file.delete()
//            val documentUri = MediaStore.getDocumentUri(applicationContext, moveFile.uri)!!
//            val documentFile = DocumentFile.fromSingleUri(applicationContext, documentUri)!!
//            val deleted = documentFile.delete()
//            val deleted = DocumentFileCompat.fromFullPath(applicationContext, moveFile.data.absPath)
//                .also { i { "documentFile=$it" } }?.delete()
        val mediaFile = moveFile.getMediaFile(context)
            .also {
                if (it == null) {
                    e { "mediaFile=null" }
                }
            }

        val fileDeleted = mediaFile?.delete()

        context.showToast(
            text = context.getString(if (fileDeleted == true) R.string.successfully_deleted_file else R.string.couldn_t_delete_file),
            duration = Toast.LENGTH_LONG
        )

        appNotificationsManager.newMoveFileNotificationManager.cancelNotification(
            intent.getParcelableCompat<NotificationResources>(
                NotificationResources.EXTRA
            )!!
        )
    }
}