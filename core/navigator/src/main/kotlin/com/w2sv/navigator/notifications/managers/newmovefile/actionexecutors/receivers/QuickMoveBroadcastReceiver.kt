package com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.FileCallback
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.core.navigator.R
import com.w2sv.domain.usecase.InsertMoveEntryUseCase
import com.w2sv.navigator.model.MoveFile
import com.w2sv.navigator.model.getMoveEntry
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.newmovefile.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.showFileSuccessfullyMovedToast
import com.w2sv.navigator.notifications.putNavigatableFileExtra
import com.w2sv.navigator.notifications.putNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
internal class QuickMoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var insertMoveEntryUseCase: InsertMoveEntryUseCase

    @Inject
    @GlobalScope(AppDispatcher.IO)
    lateinit var scope: CoroutineScope

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
        val moveFile =
            intent.getParcelableCompat<MoveFile>(MoveFile.EXTRA)!!

        if (!moveFile.mediaStoreFile.columnData.fileExists) {
            NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
                context,
                intent
            )
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
            ) ?: return context.showToast(R.string.couldnt_move_file_internal_error)

        val moveMediaFile = moveFile.getSimpleStorageMediaFile(context)
            ?: return context.showToast(R.string.couldnt_move_file_internal_error)

        // Exit if file already at selected location.
        if (targetDirectoryDocumentFile.hasChild(
                context = context,
                path = moveFile.mediaStoreFile.columnData.name,
                requiresWriteAccess = false
            )
        ) {
            return context.showToast(R.string.file_already_at_selected_location)
        }

        scope.launch {
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
        }
    }

    companion object {
        fun getIntent(
            moveFile: MoveFile,
            notificationResources: NotificationResources,
            moveDestination: Uri,
            context: Context
        ): Intent =
            Intent(context, QuickMoveBroadcastReceiver::class.java)
                .putNavigatableFileExtra(moveFile)
                .putNotificationResourcesExtra(notificationResources)
                .putExtra(
                    EXTRA_MOVE_DESTINATION,
                    moveDestination
                )

        private const val EXTRA_MOVE_DESTINATION =
            "com.w2sv.filenavigator.extra.MOVE_DESTINATION"
    }
}

@AndroidEntryPoint
internal class MoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var insertMoveEntryUseCase: InsertMoveEntryUseCase

    @Inject
    @GlobalScope(AppDispatcher.IO)
    lateinit var scope: CoroutineScope

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
        val moveFile =
            intent.getParcelableCompat<MoveFile>(MoveFile.EXTRA)!!

        if (!moveFile.mediaStoreFile.columnData.fileExists) {
            NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
                context,
                intent
            )
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
            ) ?: return context.showToast(R.string.couldnt_move_file_internal_error)

        val moveMediaFile = moveFile.getSimpleStorageMediaFile(context)
            ?: return context.showToast(R.string.couldnt_move_file_internal_error)

        // Exit if file already at selected location.
        if (targetDirectoryDocumentFile.hasChild(
                context = context,
                path = moveFile.mediaStoreFile.columnData.name,
                requiresWriteAccess = false
            )
        ) {
            return context.showToast(R.string.file_already_at_selected_location)
        }

        scope.launch {
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
        }
    }

    companion object {
        fun getIntent(
            moveFile: MoveFile,
            notificationResources: NotificationResources,
            moveDestination: Uri,
            context: Context
        ): Intent =
            Intent(context, QuickMoveBroadcastReceiver::class.java)
                .putNavigatableFileExtra(moveFile)
                .putNotificationResourcesExtra(notificationResources)
                .putExtra(
                    EXTRA_MOVE_DESTINATION,
                    moveDestination
                )

        private const val EXTRA_MOVE_DESTINATION =
            "com.w2sv.filenavigator.extra.MOVE_DESTINATION"
    }
}