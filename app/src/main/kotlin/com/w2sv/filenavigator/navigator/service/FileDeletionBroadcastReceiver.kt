package com.w2sv.filenavigator.navigator.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.filenavigator.navigator.MoveFile
import com.w2sv.filenavigator.utils.isExternalStorageManger
import slimber.log.e

class FileDeletionBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (!isExternalStorageManger()) {
            context.showToast(
                "File deletion requires access to manage all files",
                duration = Toast.LENGTH_LONG
            )
            return
        }

        val moveFile =
            intent.getParcelableCompat<MoveFile>(FileNavigatorService.EXTRA_MOVE_FILE)!!
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
            text = if (fileDeleted == true) "Successfully deleted file" else "Couldn't delete file",
            duration = Toast.LENGTH_LONG
        )

        intent
            .getParcelableCompat<MoveFile.NotificationParameters>(MoveFile.NotificationParameters.EXTRA)!!
            .cancelUnderlyingNotification(context)
    }
}