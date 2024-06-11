package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.FileCallback
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.fileName
import com.w2sv.common.utils.getText
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.common.utils.showToast
import com.w2sv.core.navigator.R
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.usecase.InsertMoveEntryUseCase
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.putMoveFileExtra
import com.w2sv.navigator.notifications.putOptionalNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
internal class MoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    internal lateinit var insertMoveEntryUseCase: InsertMoveEntryUseCase

    @Inject
    internal lateinit var navigatorConfigDataSource: NavigatorConfigDataSource

    @Inject
    @GlobalScope(AppDispatcher.IO)
    internal lateinit var scope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        moveFile(context, intent)?.let { moveException ->
            onMoveException(moveException = moveException, context = context, intent = intent)
        }
    }

    private fun moveFile(context: Context, intent: Intent): MoveException? {
        // Exit if 'manage all files' permission not granted
        if (!isExternalStorageManger) {
            return MoveException.MissingManageAllFilesPermission
        }

        // Extract extras
        val moveFile = MoveFile.fromIntent(intent)

        if (!moveFile.mediaStoreFile.columnData.fileExists) {
            return MoveException.MoveFileNotFound
        }

        // Exit on unsuccessful conversion to SimpleStorage objects
        val moveDestinationDocumentFile =
            DocumentFile.fromSingleUri(
                context,
                moveFile.moveMode!!.destination.also {
                    i { "Received move destination: $it" }
                }

            )
        val moveMediaFile = moveFile.getSimpleStorageMediaFile(context)

        if (moveDestinationDocumentFile == null || moveMediaFile == null) {
            return MoveException.InternalError
        }

        // Exit if file already at selected location.
        if (moveDestinationDocumentFile.hasChild(
                context = context,
                path = moveFile.mediaStoreFile.columnData.name,
                requiresWriteAccess = false
            )
        ) {
            return MoveException.FileAlreadyAtMoveDestination
        }

        moveMediaFile.moveTo(
            targetFolder = moveDestinationDocumentFile,
            callback = object : FileCallback() {
                override fun onCompleted(result: Any) {
                    context.showFileSuccessfullyMovedToast(moveDestinationDocumentFile)

                    scope.launch {
                        val movedFileDocumentUri =
                            Uri.parse("${moveDestinationDocumentFile.uri}%2F${Uri.encode(moveFile.mediaStoreFile.columnData.name)}")
                        insertMoveEntryUseCase(
                            moveFile.getMoveEntry(
                                destinationDocumentUri = moveDestinationDocumentFile.uri,
                                movedFileDocumentUri = movedFileDocumentUri,
                                movedFileMediaUri = MediaStore.getMediaUri(
                                    context,
                                    movedFileDocumentUri
                                )!!,
                                dateTime = LocalDateTime.now(),
                                autoMoved = moveFile.moveMode is MoveMode.Auto
                            )
                        )

                        if (moveFile.moveMode.updateLastMoveDestinations) {
                            navigatorConfigDataSource.saveLastMoveDestination(
                                fileType = moveFile.fileType,
                                sourceType = moveFile.sourceType,
                                destination = moveDestinationDocumentFile.uri
                            )
                        }
                    }

                    if (intent.hasExtra(NotificationResources.EXTRA)) {
                        NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
                            context = context,
                            intent = intent
                        )
                    }
                }

                override fun onFailed(errorCode: ErrorCode) {
                    i { errorCode.toString() }
                    context.showToast(errorCode.name)
                }
            }
        )
        return null
    }

    companion object {
        fun sendBroadcast(
            context: Context,
            moveFile: MoveFile,
            notificationResources: NotificationResources? = null
        ) {
            context.sendBroadcast(
                getIntent(
                    moveFile = moveFile,
                    notificationResources = notificationResources,
                    context = context
                )
            )
        }

        fun sendBroadcast(
            context: Context,
            fileMoveActivityIntent: Intent,
        ) {
            context.sendBroadcast(
                fileMoveActivityIntent.apply {
                    setClass(context, MoveBroadcastReceiver::class.java)
                }
            )
        }

        fun getIntent(
            moveFile: MoveFile,
            notificationResources: NotificationResources?,
            context: Context
        ): Intent =
            Intent(context, MoveBroadcastReceiver::class.java)
                .putMoveFileExtra(moveFile)
                .putOptionalNotificationResourcesExtra(notificationResources)
    }
}

//private fun movedFileMediaUri(
//    moveDestinationDocumentFile: DocumentFile,
//    fileName: String,
//    context: Context
//): Uri? {
//    val documentFile = moveDestinationDocumentFile
//        .child(context, fileName, false)
//        ?: return null
//
//    i { "URI movedFileDocumentUri: ${documentFile.uri}" }
//
//    val mediaUri = MediaStore.getMediaUri(
//        context,
//        documentFile.uri
//    )
//
//    i { "URI moved Media: $mediaUri" }
//
//    return mediaUri
//}

//private fun movedFileMediaUri(
//    moveDestinationDocumentUri: Uri,
//    fileName: String,
//    context: Context
//): Uri? {
//    val movedFileDocumentUri =
//        Uri.parse("$moveDestinationDocumentUri%2F${Uri.encode(fileName)}")
//
//    return MediaStore.getMediaUri(
//        context,
//        movedFileDocumentUri
//    )
//}

private fun Context.showFileSuccessfullyMovedToast(targetDirectory: DocumentFile) {
    showToast(
        getText(
            R.string.moved_file_to,
            "/${targetDirectory.fileName(this@showFileSuccessfullyMovedToast)}"
        )
    )
}

private fun onMoveException(moveException: MoveException, context: Context, intent: Intent) {
    moveException.toastProperties.let {
        context.showToast(it)
    }
    if (moveException.cancelNotification) {
        NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
            context,
            intent
        )
    }
}