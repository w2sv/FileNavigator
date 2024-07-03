package com.w2sv.navigator.moving

import android.content.Context
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
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveException
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.AutoMoveDestinationInvalidNotificationManager
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FileMover @Inject constructor(
    private val insertMoveEntryUseCase: InsertMoveEntryUseCase,
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val newMoveFileNotificationManager: NewMoveFileNotificationManager,
    private val autoMoveDestinationInvalidNotificationManager: AutoMoveDestinationInvalidNotificationManager,
    @GlobalScope(AppDispatcher.IO) private val scope: CoroutineScope
) {
    operator fun invoke(
        moveBundle: MoveBundle,
        notificationResources: NotificationResources?,
        context: Context
    ) {
        attemptMove(
            moveBundle = moveBundle,
            notificationResources = notificationResources,
            context = context
        )
            ?.let { moveException ->
                onMoveException(
                    context = context,
                    moveException = moveException,
                    notificationResources = notificationResources
                )
            }
    }

    private fun attemptMove(
        moveBundle: MoveBundle,
        notificationResources: NotificationResources?,
        context: Context
    ): MoveException? {
        // Exit if 'manage all files' permission not granted
        if (!isExternalStorageManger) {
            return MoveException.MissingManageAllFilesPermission
        }

        if (!moveBundle.file.mediaStoreData.fileExists) {
            return MoveException.MoveFileNotFound
        }

        // Exit on unsuccessful conversion to SimpleStorage objects
        val moveDestinationDocumentFile = moveBundle.destination.documentFile(context)
        val moveMediaFile = moveBundle.file.simpleStorageMediaFile(context)

        if (moveDestinationDocumentFile == null || moveMediaFile == null) {
            return MoveException.InternalError
        }

        // Exit if file already at selected location.
        if (moveDestinationDocumentFile.hasChild(
                context = context,
                path = moveBundle.file.mediaStoreData.name,
                requiresWriteAccess = false
            )
        ) {
            return MoveException.FileAlreadyAtMoveDestination
        }

        moveMediaFile.moveTo(
            targetFolder = moveDestinationDocumentFile,
            callback = object : FileCallback() {  // onReport override not being called for some reason

                /**
                 * @param result androidx.documentfile.providerRawDocumentFile with uri of form "file:///storage/emulated/0/Moved/Screenshots/Screenshot_2024-07-03-17-02-26-344_com.w2sv.filenavigator.debug.jpg".
                 */
                override fun onCompleted(result: Any) {
                    context.showMoveSuccessToast(
                        moveBundle = moveBundle,
                        moveDestinationDocumentFile = moveDestinationDocumentFile
                    )

                    scope.launch {
                        val movedFileDocumentUri = movedFileDocumentUri(
                            moveDestinationDocumentUri = moveBundle.destination,
                            fileName = moveBundle.file.mediaStoreData.name
                        )
                        insertMoveEntryUseCase(
                            moveBundle.moveEntry(
                                movedFileDocumentUri = movedFileDocumentUri,
                                movedFileMediaUri = movedFileDocumentUri.mediaUri(context)!!,
                                dateTime = LocalDateTime.now(),
                            )
                        )

                        if (moveBundle.mode.updateLastMoveDestinations) {
                            i { "Saving last move destination" }
                            navigatorConfigDataSource.saveLastMoveDestination(
                                fileType = moveBundle.file.fileType,
                                sourceType = moveBundle.file.sourceType,
                                destination = moveBundle.destination
                            )
                        }
                    }

                    notificationResources?.let {
                        NotificationResources.CleanupBroadcastReceiver.start(context, it)
                    }
                }

                override fun onFailed(errorCode: ErrorCode) {
                    i { errorCode.toString() }

                    if (errorCode == ErrorCode.TARGET_FOLDER_NOT_FOUND && moveBundle.mode.isAuto) {
                        scope.launch {
                            navigatorConfigDataSource.unsetAutoMoveConfig(
                                fileType = moveBundle.file.fileType,
                                sourceType = moveBundle.file.sourceType
                            )
                            FileNavigator.reregisterFileObservers(context)
                        }
                        with(newMoveFileNotificationManager) {
                            buildAndEmit(
                                BuilderArgs(
                                    moveFile = moveBundle.file
                                )
                            )
                        }
                        with(autoMoveDestinationInvalidNotificationManager) {
                            buildAndEmit(
                                BuilderArgs(
                                    fileAndSourceType = moveBundle.file.fileAndSourceType,
                                    autoMoveDestination = moveBundle.destination
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
            id = if (moveBundle.mode.isAuto) R.string.auto_move_success_toast_text else R.string.move_success_toast_text,
            moveBundle.file.fileAndSourceType.label(context = this, isGif = moveBundle.file.isGif),
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

private fun onMoveException(
    context: Context,
    moveException: MoveException,
    notificationResources: NotificationResources?
) {
    context.showToast(moveException.toastProperties)
    if (moveException.cancelNotification) {
        notificationResources?.let {
            NotificationResources.CleanupBroadcastReceiver.start(context, notificationResources)
        }
    }
}