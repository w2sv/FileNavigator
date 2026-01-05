package com.w2sv.navigator.notifications

import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import com.w2sv.navigator.notifications.controller.AutoMoveDestinationInvalidNotificationController
import com.w2sv.navigator.notifications.controller.BatchMoveNotificationController
import com.w2sv.navigator.notifications.controller.BatchMoveNotificationControllerArgs
import com.w2sv.navigator.notifications.controller.BatchMoveProgressNotificationController
import com.w2sv.navigator.notifications.controller.BatchMoveResultsNotificationController
import com.w2sv.navigator.notifications.controller.MoveFileNotificationController
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class NotificationEventHandlerImpl @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,
    private val moveFileNotificationController: MoveFileNotificationController,
    private val batchMoveNotificationController: BatchMoveNotificationController,
    private val batchMoveProgressNotificationController: BatchMoveProgressNotificationController,
    private val batchMoveResultsNotificationController: BatchMoveResultsNotificationController,
    private val autoMoveDestinationInvalidNotificationController: AutoMoveDestinationInvalidNotificationController
) : NotificationEventHandler {

    private val showBatchMoveNotification =
        navigatorConfigDataSource
            .navigatorConfig
            .map { it.showBatchMoveNotification }
            .stateIn(scope, SharingStarted.Eagerly, false)

    override operator fun invoke(event: NotificationEvent) {
        when (event) {
            is NotificationEvent.PostMoveFile -> onPostMoveFile(event)
            is NotificationEvent.CancelMoveFile -> onCancelMoveFile(event)
            is NotificationEvent.AutoMoveDestinationInvalid -> onAutoMoveDestinationInvalid(event)
            is NotificationEvent.CancelAutoMoveDestinationInvalid -> onCancelAutoMoveDestinationInvalid(event)
            is NotificationEvent.BatchMoveProgress -> onBatchMoveProgress(event)
            is NotificationEvent.BatchMoveResults -> onBatchMoveResults(event)
        }
    }

    private fun onBatchMoveResults(event: NotificationEvent.BatchMoveResults) {
        batchMoveResultsNotificationController.post(event)
    }

    private fun onBatchMoveProgress(event: NotificationEvent.BatchMoveProgress) {
        batchMoveProgressNotificationController.post(event)
    }

    private fun onCancelAutoMoveDestinationInvalid(event: NotificationEvent.CancelAutoMoveDestinationInvalid) {
        autoMoveDestinationInvalidNotificationController.cancel(event.id)
    }

    private fun onAutoMoveDestinationInvalid(event: NotificationEvent.AutoMoveDestinationInvalid) {
        autoMoveDestinationInvalidNotificationController.post(event)
    }

    private fun onPostMoveFile(event: NotificationEvent.PostMoveFile) {
        moveFileNotificationController.post(event.moveFile)
        if (showBatchMoveNotification.value && moveFileNotificationController.notificationCount > 1) {
            batchMoveNotificationController.post(batchMoveNotificationArgs())
        }
    }

    private fun onCancelMoveFile(event: NotificationEvent.CancelMoveFile) {
        moveFileNotificationController.cancel(event.id)
        // TODO: track whether single instance notification is showing or not
        if (showBatchMoveNotification.value) {
            batchMoveNotificationController.cancelOrUpdate(batchMoveNotificationArgs())
        }
    }

    private fun batchMoveNotificationArgs(): BatchMoveNotificationControllerArgs =
        moveFileNotificationController.idToArgs.mapKeys { (id, _) -> NotificationEvent.CancelMoveFile(id) }
}
