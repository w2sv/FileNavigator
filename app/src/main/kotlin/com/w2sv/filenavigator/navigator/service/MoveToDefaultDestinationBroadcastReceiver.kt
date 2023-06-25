package com.w2sv.filenavigator.navigator.service

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
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.navigator.MoveFile
import com.w2sv.filenavigator.utils.isExternalStorageManger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoveToDefaultDestinationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (!isExternalStorageManger()) {
            context.showToast(
                "Moving to the default destination requires permission to manage all files!",
                duration = Toast.LENGTH_LONG
            )
        }

        val moveFile =
            intent.getParcelableCompat<MoveFile>(FileNavigatorService.EXTRA_MOVE_FILE)!!
        val defaultMoveDestination =
            intent.getParcelableCompat<Uri>(FileNavigatorService.EXTRA_DEFAULT_MOVE_DESTINATION)!!

        // Exit on unsuccessful conversion to SimpleStorage objects
        val targetDirectoryDocumentFile =
            DocumentFile.fromSingleUri(context, defaultMoveDestination)
        val moveMediaFile = moveFile.getMediaFile(context)

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

        intent
            .getParcelableCompat<MoveFile.NotificationParameters>(MoveFile.NotificationParameters.EXTRA)!!
            .cancelUnderlyingNotification(context)
    }
}