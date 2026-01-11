package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.content.intent
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.navigator.di.MoveOperationSummaryChannel
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.MoveOperationSummary
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
internal class MoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var moveOperationSummaryChannel: MoveOperationSummaryChannel

    @Inject
    @GlobalScope(AppDispatcher.IO)
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val operation = MoveOperation<MoveOperation>(intent)

        scope.launch {
            operation.file.moveTo(destination = operation.destination, context = context) { result ->
                moveOperationSummaryChannel.trySend(MoveOperationSummary(result, operation))
            }
        }
    }

    companion object {
        fun sendBroadcast(operation: MoveOperation, context: Context) {
            context.sendBroadcast(
                intent(
                    moveBundle = operation,
                    context = context
                )
            )
        }

        fun intent(moveBundle: MoveOperation, context: Context): Intent =
            intent<MoveBroadcastReceiver>(context)
                .putExtra(MoveOperation.EXTRA, moveBundle)
    }
}
