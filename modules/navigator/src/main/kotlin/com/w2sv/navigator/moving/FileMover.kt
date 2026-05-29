package com.w2sv.navigator.moving

import android.content.Context
import com.w2sv.common.di.ApplicationIoScope
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.postmove.MoveOperationSummary
import com.w2sv.navigator.postmove.MoveSummaryChannel
import com.w2sv.navigator.postmove.loggingTrySend
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class FileMover @Inject constructor(
    private val moveSummaryChannel: MoveSummaryChannel,
    @ApplicationIoScope private val scope: CoroutineScope
) {
    operator fun invoke(operation: MoveOperation, context: Context) {
        scope.launch {
            operation.file.moveTo(destination = operation.destination, context = context) { result ->
                moveSummaryChannel.loggingTrySend(MoveOperationSummary(result, operation))
            }
        }
    }
}
