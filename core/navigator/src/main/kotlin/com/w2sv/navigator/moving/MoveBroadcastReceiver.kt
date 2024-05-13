package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.FileCallback
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.common.utils.showToast
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.domain.usecase.InsertMoveEntryUseCase
import com.w2sv.navigator.model.MoveFile
import com.w2sv.navigator.model.getMoveEntry
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.newmovefile.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.putMoveFileExtra
import com.w2sv.navigator.notifications.putNotificationResourcesExtra
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
    internal lateinit var navigatorRepository: NavigatorRepository

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
        if (!isExternalStorageManger()) {
            return MoveException.MissingManageAllFilesPermission
        }

        // Extract extras
        val moveFile =
            intent.getParcelableCompat<MoveFile>(MoveFile.EXTRA)!!

        if (!moveFile.mediaStoreFile.columnData.fileExists) {
            return MoveException.MoveFileNotFound
        }

        // Exit on unsuccessful conversion to SimpleStorage objects
        val targetDirectoryDocumentFile =
            DocumentFile.fromSingleUri(
                context,
                intent.getParcelableCompat<Uri>(Extra.MOVE_DESTINATION)!!
                    .also {
                        i { "Received move destination: $it" }
                    }
            )
        val moveMediaFile = moveFile.getSimpleStorageMediaFile(context)

        if (targetDirectoryDocumentFile == null || moveMediaFile == null) {
            return MoveException.InternalError
        }

        // Exit if file already at selected location.
        if (targetDirectoryDocumentFile.hasChild(
                context = context,
                path = moveFile.mediaStoreFile.columnData.name,
                requiresWriteAccess = false
            )
        ) {
            return MoveException.FileAlreadyAtMoveDestination
        }

        val updateLastMoveDestination = intent.hasExtra(Extra.UPDATE_LAST_MOVE_DESTINATION)

        moveMediaFile.moveTo(
            targetFolder = targetDirectoryDocumentFile,
            callback = object : FileCallback() {
                override fun onCompleted(result: Any) {
                    context.showFileSuccessfullyMovedToast(targetDirectoryDocumentFile)

                    scope.launch {
                        insertMoveEntryUseCase(
                            getMoveEntry(
                                moveFile,
                                targetDirectoryDocumentFile.uri,
                                LocalDateTime.now()
                            )
                        )
                        if (updateLastMoveDestination) {
                            navigatorRepository.saveLastMoveDestination(
                                source = moveFile.source,
                                destination = targetDirectoryDocumentFile.uri
                            )
                        }
                    }

                    NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
                        context,
                        intent
                    )
                }

                override fun onFailed(errorCode: ErrorCode) {
                    i { errorCode.toString() }
                    context.showToast(errorCode.name)
                }
            }
        )
        return null
    }

    private object Extra {
        const val MOVE_DESTINATION =
            "com.w2sv.filenavigator.extra.MOVE_DESTINATION"
        const val UPDATE_LAST_MOVE_DESTINATION =
            "com.w2sv.filenavigator.extra.UPDATE_LAST_MOVE_DESTINATION"
    }

    companion object {
        fun getIntent(
            moveFile: MoveFile,
            moveDestinationDocumentUri: Uri,
            notificationResources: NotificationResources,
            context: Context
        ): Intent =
            Intent(context, MoveBroadcastReceiver::class.java)
                .putMoveFileExtra(moveFile)
                .putNotificationResourcesExtra(notificationResources)
                .putMoveDestinationExtra(moveDestinationDocumentUri)

        fun startFromFileMoveActivityIntent(
            context: Context,
            fileMoveActivityIntent: Intent,
            moveDestinationDocumentUri: Uri
        ) {
            context.sendBroadcast(
                fileMoveActivityIntent.apply {
                    setClass(context, MoveBroadcastReceiver::class.java)
                    putMoveDestinationExtra(moveDestinationDocumentUri)
                    putExtra(Extra.UPDATE_LAST_MOVE_DESTINATION, true)
                }
            )
        }

        private fun Intent.putMoveDestinationExtra(moveDestinationDocumentUri: Uri): Intent =
            putExtra(
                Extra.MOVE_DESTINATION,
                moveDestinationDocumentUri
            )
    }
}

private fun onMoveException(moveException: MoveException, context: Context, intent: Intent) {
    moveException.toastProperties?.let {
        context.showToast(it)
    }
    if (moveException.cancelNotification) {
        NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
            context,
            intent
        )
    }
}