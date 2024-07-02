package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.FileCallback
import com.w2sv.androidutils.res.getText
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.fileName
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.common.utils.showToast
import com.w2sv.core.navigator.R
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.usecase.InsertMoveEntryUseCase
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.AutoMoveDestinationInvalidNotificationManager
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
    internal lateinit var newMoveFileNotificationManager: NewMoveFileNotificationManager

    @Inject
    lateinit var autoMoveDestinationInvalidNotificationManager: AutoMoveDestinationInvalidNotificationManager

    @Inject
    @GlobalScope(AppDispatcher.IO)
    internal lateinit var scope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        moveFile(context, intent)?.let { moveException ->
            onMoveException(
                moveException = moveException,
                context = context,
                intent = intent
            )
        }
    }

    private fun moveFile(context: Context, intent: Intent): MoveException? {
        // Exit if 'manage all files' permission not granted
        if (!isExternalStorageManger) {
            return MoveException.MissingManageAllFilesPermission
        }

        // Extract extras
        val moveBundle = MoveBundle.fromIntent(intent)

        if (!moveBundle.moveFile.mediaStoreData.fileExists) {
            return MoveException.MoveFileNotFound
        }

        // Exit on unsuccessful conversion to SimpleStorage objects
        val moveDestinationDocumentFile = moveBundle.moveMode!!.destination.documentFile(context)
        val moveMediaFile = moveBundle.simpleStorageMediaFile(context)

        if (moveDestinationDocumentFile == null || moveMediaFile == null) {
            return MoveException.InternalError
        }

        // Exit if file already at selected location.
        if (moveDestinationDocumentFile.hasChild(
                context = context,
                path = moveBundle.moveFile.mediaStoreData.name,
                requiresWriteAccess = false
            )
        ) {
            return MoveException.FileAlreadyAtMoveDestination
        }

        moveMediaFile.moveTo(
            targetFolder = moveDestinationDocumentFile,
            callback = object : FileCallback() {
                override fun onCompleted(result: Any) {
                    context.showMoveSuccessToast(
                        moveBundle = moveBundle,
                        moveDestinationDocumentFile = moveDestinationDocumentFile
                    )

                    scope.launch {
                        val movedFileDocumentUri = movedFileDocumentUri(
                            moveDestinationDocumentUri = moveBundle.moveMode.destination,
                            fileName = moveBundle.moveFile.mediaStoreData.name
                        )
                        insertMoveEntryUseCase(
                            moveBundle.moveEntry(
                                destinationDocumentUri = moveBundle.moveMode.destination,
                                movedFileDocumentUri = movedFileDocumentUri,
                                movedFileMediaUri = movedFileDocumentUri.mediaUri(context)!!,
                                dateTime = LocalDateTime.now(),
                                autoMoved = moveBundle.moveMode.isAuto
                            )
                        )

                        if (moveBundle.moveMode.updateLastMoveDestinations) {
                            navigatorConfigDataSource.saveLastMoveDestination(
                                fileType = moveBundle.fileType,
                                sourceType = moveBundle.sourceType,
                                destination = moveBundle.moveMode.destination
                            )
                        }
                    }

                    if (intent.hasExtra(NotificationResources.EXTRA)) {
                        NotificationResources.CleanupBroadcastReceiver.startFromResourcesComprisingIntent(
                            context = context,
                            intent = intent
                        )
                    }
                }

                override fun onFailed(errorCode: ErrorCode) {
                    i { errorCode.toString() }

                    if (errorCode == ErrorCode.TARGET_FOLDER_NOT_FOUND && moveBundle.moveMode.isAuto) {
                        scope.launch {
                            navigatorConfigDataSource.unsetAutoMoveConfig(
                                fileType = moveBundle.fileType,
                                sourceType = moveBundle.sourceType
                            )
                            FileNavigator.reregisterFileObservers(context)
                        }
                        with(newMoveFileNotificationManager) {
                            buildAndEmit(
                                BuilderArgs(
                                    moveBundle = moveBundle.copy(moveMode = null)
                                )
                            )
                        }
                        with(autoMoveDestinationInvalidNotificationManager) {
                            buildAndEmit(
                                BuilderArgs(
                                    fileAndSourceType = moveBundle.fileAndSourceType,
                                    autoMoveDestination = moveBundle.moveMode.destination
                                )
                            )
                        }
                    } else {
                        context.showToast(errorCode.name)
                    }
                }
            }
        )
        return null
    }

    companion object {
        fun sendBroadcast(
            context: Context,
            moveBundle: MoveBundle,
            notificationResources: NotificationResources? = null
        ) {
            context.sendBroadcast(
                getIntent(
                    moveBundle = moveBundle,
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
            moveBundle: MoveBundle,
            notificationResources: NotificationResources?,
            context: Context
        ): Intent =
            Intent(context, MoveBroadcastReceiver::class.java)
                .putMoveFileExtra(moveBundle)
                .putOptionalNotificationResourcesExtra(notificationResources)
    }
}

private fun movedFileDocumentUri(
    moveDestinationDocumentUri: DocumentUri,
    fileName: String
): DocumentUri =
    DocumentUri.parse("$moveDestinationDocumentUri%2F${Uri.encode(fileName)}")

private fun Context.showMoveSuccessToast(
    moveBundle: MoveBundle,
    moveDestinationDocumentFile: DocumentFile
) {
    showToast(
        resources.getText(
            if (moveBundle.moveMode!!.isAuto) R.string.auto_move_success_toast_text else R.string.move_success_toast_text,
            moveBundle.fileAndSourceType.label(context = this, isGif = moveBundle.isGif),
            moveDestinationRepresentation(
                this@showMoveSuccessToast,
                moveDestinationDocumentFile
            )
        )
    )
}

private fun moveDestinationRepresentation(
    context: Context,
    moveDestinationDocumentFile: DocumentFile
): String =
    "/${moveDestinationDocumentFile.fileName(context)}"

private fun onMoveException(moveException: MoveException, context: Context, intent: Intent) {
    context.showToast(moveException.toastProperties)
    if (moveException.cancelNotification) {
        NotificationResources.CleanupBroadcastReceiver.startFromResourcesComprisingIntent(
            context,
            intent
        )
    }
}