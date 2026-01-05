package com.w2sv.navigator.domain.moving

import android.content.Context
import com.w2sv.navigator.domain.notifications.CancelNotificationEvent

sealed interface MoveOperationSummary {
    val result: MoveResult
    val operation: MoveOperation? get() = null
    val cancelMoveFileNotificationEvent: CancelNotificationEvent?
    val makeFeedback: (Context) -> MoveOperationFeedback?

    @JvmInline
    value class PreLaunchError(override val result: MoveResult) : MoveOperationSummary {
        override val cancelMoveFileNotificationEvent: CancelNotificationEvent?
            get() = null

        override val makeFeedback: (Context) -> MoveOperationFeedback?
            get() = { context -> result.makeFeedback(context, null) }
    }

    data class WithOperation(override val result: MoveResult, override val operation: MoveOperation) : MoveOperationSummary {
        override val cancelMoveFileNotificationEvent: CancelNotificationEvent?
            get() = operation.destinationSelectionManner.cancelNotificationEvent.takeIf { result.cancelNotification }

        override val makeFeedback: (Context) -> MoveOperationFeedback?
            get() = { context -> result.makeFeedback(context, operation) }
    }

    companion object {
        operator fun invoke(result: MoveResult, operation: MoveOperation? = null): MoveOperationSummary =
            operation?.let { WithOperation(result, it) } ?: PreLaunchError(result)
    }
}
