package com.w2sv.navigator.moving

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.w2sv.androidutils.res.getText
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.fileName
import com.w2sv.core.navigator.R
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.usecase.InsertMoveEntryUseCase
import com.w2sv.kotlinutils.coroutines.collectFromFlow
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.AutoMoveDestinationInvalidNotificationManager
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MoveResultConsumer @Inject constructor(
    private val moveResultFlow: MoveResultFlow,
    private val insertMoveEntryUseCase: InsertMoveEntryUseCase,
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val newMoveFileNotificationManager: NewMoveFileNotificationManager,
    private val autoMoveDestinationInvalidNotificationManager: AutoMoveDestinationInvalidNotificationManager,
    @GlobalScope(AppDispatcher.IO) private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) {
    init {
        scope.collectFromFlow(moveResultFlow) { moveResult ->
            when (moveResult) {
                is MoveResult.Success -> {
                    onSuccess(moveResult.moveBundle)
                }
            }
        }
    }

    private fun onSuccess(moveBundle: MoveBundle) {
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