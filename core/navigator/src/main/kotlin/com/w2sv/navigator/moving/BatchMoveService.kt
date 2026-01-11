package com.w2sv.navigator.moving

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.content.getParcelableCompat
import com.w2sv.androidutils.content.intent
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.logging.LoggingUnboundService
import com.w2sv.navigator.di.MoveOperationSummaryChannel
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.MoveOperationSummary
import com.w2sv.navigator.domain.moving.MoveResult
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import slimber.log.e
import slimber.log.w

@AndroidEntryPoint
internal class BatchMoveService : LoggingUnboundService() {

    @Inject
    lateinit var notificationEventHandler: NotificationEventHandler

    @Inject
    lateinit var moveOperationSummaryChannel: MoveOperationSummaryChannel

    @Inject
    @GlobalScope(AppDispatcher.Default)
    lateinit var scope: CoroutineScope

    private var batchMoveJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logOnStartCommand(intent)

        when (val action = intent?.action) {
            ACTION_START -> onStartActionReceived(intent)
            ACTION_CANCEL -> cancelBatchMove()
            else -> w { "Service started with unknown action: $action" }
        }

        return START_NOT_STICKY
    }

    private fun onStartActionReceived(intent: Intent) {
        if (batchMoveJob?.isActive == true) {
            w { "Received ACTION_START while batch move still in progress" }
            return
        }
        val args = intent.getParcelableCompat<Args>(Args.EXTRA) ?: run {
            e { "Couldn't retrieve args from intent" }
            return
        }

        startBatchMove(args.operations, args.destination)
    }

    // ----------------------
    // Batch Move Logic
    // ----------------------

    private fun startBatchMove(bundles: List<MoveOperation.Batchable>, destination: MoveDestination.Directory) {
        batchMoveJob = scope.launch {
            val total = bundles.size
            notifyBatchProgress(current = 0, total = total)

            val moveResults = performBatchMove(
                bundles = bundles,
                destination = destination,
                context = applicationContext,
                onResult = { index, summary ->
                    notifyBatchProgress(current = index + 1, total = total)
                    moveOperationSummaryChannel.trySend(summary)
                }
            )

            notifyBatchFinished(moveResults, destination)
            stopSelf()
        }
    }

    private suspend fun CoroutineScope.performBatchMove(
        bundles: List<MoveOperation.Batchable>,
        destination: MoveDestination.Directory,
        context: Context,
        onResult: (Int, MoveOperationSummary) -> Unit
    ): List<MoveResult> {
        val destinationDocumentFile = destination.documentFile(context)
        return buildList {
            try {
                bundles.forEachIndexed { index, operation ->
                    ensureActive()

                    operation.file.moveTo(
                        destination = destination,
                        destinationDocumentFile = destinationDocumentFile,
                        context = context
                    ) { result ->
                        onResult(index, MoveOperationSummary(result, operation))
                        add(result)
                    }
                }
            } catch (_: CancellationException) {
            }
        }
    }

    private fun notifyBatchProgress(current: Int, total: Int) {
        notificationEventHandler(
            NotificationEvent.BatchMoveProgress(
                current = current,
                total = total
            )
        )
    }

    private fun notifyBatchFinished(moveResults: List<MoveResult>, destination: MoveDestination.Directory) {
        notificationEventHandler(
            NotificationEvent.BatchMoveResults(
                moveResults = moveResults,
                destination = destination
            )
        )
    }

    // ----------------------
    // Destroy/Cancel
    // ----------------------

    override fun onDestroy() {
        batchMoveJob?.cancel()
        super.onDestroy()
    }

    private fun cancelBatchMove() {
        batchMoveJob?.cancel()
        stopSelf()
    }

    // ----------------------
    // Companion Object
    // ----------------------

    @Parcelize
    data class Args(val operations: List<MoveOperation.Batchable>) : Parcelable {

        val destination: MoveDestination.Directory
            get() = operations.first().destination

        companion object {
            const val EXTRA = "com.w2sv.navigator.extra.BatchMoveService.Args"
        }
    }

    companion object {
        private const val ACTION_START = "batch_move_start"
        private const val ACTION_CANCEL = "batch_move_cancel"

        fun startIntent(context: Context, args: Args): Intent =
            intent<BatchMoveService>(context).apply {
                action = ACTION_START
                putExtra(Args.EXTRA, args)
            }

        fun cancelIntent(context: Context): Intent =
            intent<BatchMoveService>(context).apply {
                action = ACTION_CANCEL
            }
    }
}
