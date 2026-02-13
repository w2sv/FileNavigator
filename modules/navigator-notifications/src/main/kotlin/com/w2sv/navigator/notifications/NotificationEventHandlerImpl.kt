package com.w2sv.navigator.notifications

import com.w2sv.common.di.ApplicationIoScope
import com.w2sv.domain.model.navigatorconfig.NavigatorConfigFlow
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.kotlinutils.coroutines.flow.combineToPair
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import com.w2sv.navigator.notifications.controller.AutoMoveDestinationInvalidNotificationController
import com.w2sv.navigator.notifications.controller.BatchMoveNotificationArgs
import com.w2sv.navigator.notifications.controller.BatchMoveNotificationController
import com.w2sv.navigator.notifications.controller.BatchMoveProgressNotificationController
import com.w2sv.navigator.notifications.controller.BatchMoveResultsNotificationController
import com.w2sv.navigator.notifications.controller.NavigateFileNotificationController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class NotificationEventHandlerImpl @Inject constructor(
    navigatorConfigFlow: NavigatorConfigFlow,
    @ApplicationIoScope scope: CoroutineScope,
    private val navigateFileNotificationController: NavigateFileNotificationController,
    private val batchMoveNotificationController: BatchMoveNotificationController,
    private val batchMoveProgressNotificationController: BatchMoveProgressNotificationController,
    private val batchMoveResultsNotificationController: BatchMoveResultsNotificationController,
    private val autoMoveDestinationInvalidNotificationController: AutoMoveDestinationInvalidNotificationController
) : NotificationEventHandler {

    private val batchMoveNotificationArgs = MutableStateFlow<BatchMoveNotificationArgs>(emptyMap())

    init {
        // Collect moveFileNotification updates
        navigateFileNotificationController.updates.collectOn(scope) { event ->
            batchMoveNotificationArgs.update {
                it.copy {
                    when (event) {
                        is NavigateFileNotificationController.UpdateEvent.Cancelled -> remove(event.id)
                        is NavigateFileNotificationController.UpdateEvent.Added -> put(event.id, event.args)
                    }
                }
            }
        }

        // Reactively cancel or post batch move notifications
        combineToPair(
            navigatorConfigFlow.map { it.showBatchMoveNotification },
            batchMoveNotificationArgs
        )
            .distinctUntilChanged()
            .collectOn(scope) { (showBatchMoveNotification, idToArgs) ->
                when {
                    !showBatchMoveNotification || idToArgs.size <= 1 -> batchMoveNotificationController.cancel()
                    else -> batchMoveNotificationController.post(args = idToArgs)
                }
            }
    }

    override operator fun invoke(event: NotificationEvent) {
        i { "Received notification event $event" }

        when (event) {
            is NotificationEvent.PostNavigateFile -> onPostMoveFile(event)
            is NotificationEvent.CancelNavigateFile -> onCancelMoveFile(event)
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

    private fun onPostMoveFile(event: NotificationEvent.PostNavigateFile) {
        navigateFileNotificationController.post(event.file)
    }

    private fun onCancelMoveFile(event: NotificationEvent.CancelNavigateFile) {
        navigateFileNotificationController.cancel(event.id)
    }
}
