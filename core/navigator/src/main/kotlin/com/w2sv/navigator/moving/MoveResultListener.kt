package com.w2sv.navigator.moving

import android.content.Context
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.usecase.InsertMovedFileUseCase
import com.w2sv.navigator.domain.moving.MoveFile
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.MoveOperationFeedback
import com.w2sv.navigator.domain.moving.MoveOperationSummary
import com.w2sv.navigator.domain.moving.MoveResult
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import slimber.log.i

internal class MoveResultListener @Inject constructor(
    private val insertMovedFileUseCase: InsertMovedFileUseCase,
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val notificationEventHandler: NotificationEventHandler,
    @GlobalScope(AppDispatcher.IO) private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) {
    suspend fun onMoveResult(summary: MoveOperationSummary) {
        i { "Received $summary" }

        summary.cancelMoveFileNotificationEvent?.let(notificationEventHandler::invoke)
        summary.makeFeedback(context)?.let { feedback ->
            when (feedback) {
                is MoveOperationFeedback.Toast -> withContext(Dispatchers.Main) { context.showToast(feedback.text, feedback.duration) }
                is MoveOperationFeedback.Notification -> notificationEventHandler(feedback.event)
            }
        }

        // TODO meh
        if (summary is MoveOperationSummary.WithOperation) {
            when (summary.result) {
                is MoveResult.Success -> onSuccess(summary.operation)
                is MoveResult.MoveDestinationNotFound -> onMoveDestinationNotFound(summary.operation)
                else -> Unit
            }
        }
    }

    private fun onMoveDestinationNotFound(operation: MoveOperation) {
        when (operation) {
            is MoveOperation.QuickMove -> onQuickMoveDestinationNotFound(operation.file)
            is MoveOperation.AutoMove -> onAutoMoveDestinationNotFound(operation.file)
            else -> error("Shouldn't happen")
        }
    }

    private fun onQuickMoveDestinationNotFound(file: MoveFile) {
        scope.launch {
            navigatorConfigDataSource.unsetQuickMoveDestination(
                fileType = file.fileType,
                sourceType = file.sourceType
            )
        }
    }

    private fun onAutoMoveDestinationNotFound(file: MoveFile) {
        scope.launch {
            navigatorConfigDataSource.unsetAutoMoveConfig(
                fileType = file.fileType,
                sourceType = file.sourceType
            )
        }
    }

    private fun onSuccess(data: MoveOperation) {
        scope.launch {
            insertMovedFileUseCase(
                data.movedFile(
                    context = context,
                    dateTime = LocalDateTime.now()
                )
            )
        }
        if (data.destinationSelectionManner.isPicked) {
            data.destination.quickMoveDestination?.let {
                i { "Saving quick move destination $it" }

                scope.launch {
                    navigatorConfigDataSource.saveQuickMoveDestination(
                        fileType = data.file.fileType,
                        sourceType = data.file.sourceType,
                        destination = it
                    )
                }
            }
        }
    }
}
