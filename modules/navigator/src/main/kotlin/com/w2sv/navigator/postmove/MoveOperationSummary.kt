package com.w2sv.navigator.postmove

import android.content.Context
import com.w2sv.kotlinutils.makeIf
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.MoveOperationFeedback
import com.w2sv.navigator.domain.moving.MoveResult
import com.w2sv.navigator.domain.notifications.CancelNotificationEvent

data class MoveOperationSummary(val result: MoveResult, val operation: MoveOperation? = null) {
    val cancelNavigateFileNotificationEvent: CancelNotificationEvent?
        get() = operation?.destinationSelectionManner?.cancelNotificationEvent?.takeIf { result.cancelNotification }

    private val isPartOfBatch
        get() = operation?.isPartOfBatch == true

    /**
     * @return [MoveOperationFeedback] corresponding to the [result] if the operation is NOT part of a batch.
     */
    fun makeFeedback(context: Context): MoveOperationFeedback? =
        makeIf(!isPartOfBatch) { result.makeFeedback(context, operation) }
}
