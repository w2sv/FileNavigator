package com.w2sv.filenavigator.navigator.service

import android.content.Intent
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.services.UnboundService
import com.w2sv.filenavigator.navigator.MoveFile
import slimber.log.i
import java.io.File

class FileDeletionService : UnboundService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val moveFile =
                intent.getParcelableCompat<MoveFile>(FileNavigatorService.EXTRA_MOVE_FILE)!!
            val file = File(moveFile.data.absPath).also { i{"File exists: ${it.exists()}"} }
            val deleted = file.delete()
//            val documentUri = MediaStore.getDocumentUri(applicationContext, moveFile.uri)!!
//            val documentFile = DocumentFile.fromSingleUri(applicationContext, documentUri)!!
//            val deleted = documentFile.delete()
//            val deleted = DocumentFileCompat.fromFullPath(applicationContext, moveFile.data.absPath)
//                .also { i { "documentFile=$it" } }?.delete()
//            moveFile.getMediaFile(applicationContext).also { i { "mediaFile=$it" } }
//                ?.toDocumentFile().also { i { "documentFile=$it" } }?.delete()
            applicationContext.showToast(
                if (deleted) "Deleted file" else "Couldn't delete file"
            )

            val notificationParameters =
                intent.getParcelableCompat<MoveFile.NotificationParameters>(MoveFile.NotificationParameters.EXTRA)!!
            notificationParameters.cancelUnderlyingNotification(applicationContext)

            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }
}