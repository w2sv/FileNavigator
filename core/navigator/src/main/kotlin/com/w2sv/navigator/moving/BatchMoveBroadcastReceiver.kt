package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.MoveDestination
import com.w2sv.navigator.moving.model.BatchMoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.managers.BatchMoveProgressNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
internal class BatchMoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var moveResultListener: MoveResultListener

    @Inject
    lateinit var batchMoveProgressNotificationManager: BatchMoveProgressNotificationManager

    @Inject
    @GlobalScope(AppDispatcher.Default)
    lateinit var scope: CoroutineScope

    private var batchMoveJob: Job? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CANCEL_BATCH_MOVE) {
            batchMoveJob?.cancel()
            return
        }

        val args = intent.getParcelableCompat<Args>(Args.EXTRA)!!

        when (val preMoveCheckResult = sharedPreMoveChecks(args.destination, context)) {
            is PreMoveCheckResult.Failure -> {
                moveResultListener.onPreMoveCancellation(preMoveCheckResult.failure, null)
            }

            is PreMoveCheckResult.Success -> {
                batchMoveJob = scope.launch {
                    val moveResults = batchMove(
                        args = args,
                        destinationDocumentFile = preMoveCheckResult.documentFile,
                        context = context
                    )
                    batchMoveProgressNotificationManager.buildAndPostNotification(
                        BatchMoveProgressNotificationManager.BuilderArgs.MoveResults(
                            moveResults = moveResults,
                            destination = args.destination
                        )
                    )
                }
            }
        }
    }

    private suspend fun batchMove(
        args: Args,
        destinationDocumentFile: DocumentFile,
        context: Context
    ): List<MoveResult> = coroutineScope {
        val moveResults = mutableListOf<MoveResult>()
        batchMoveProgressNotificationManager.buildAndPostNotification(
            BatchMoveProgressNotificationManager.BuilderArgs.MoveProgress(
                current = 0,
                max = args.batchMoveBundles.size
            )
        )
        args.batchMoveBundles.forEachIndexed { index, batchMoveBundle ->
            if (isActive) {  // Make cancellable
                val moveResult = batchMoveBundle.moveFile.moveTo(
                    destination = destinationDocumentFile,
                    context = context
                )
                batchMoveProgressNotificationManager.buildAndPostNotification(
                    BatchMoveProgressNotificationManager.BuilderArgs.MoveProgress(
                        current = index + 1,
                        max = args.batchMoveBundles.size
                    )
                )
                moveResultListener.invoke(
                    moveResult = moveResult,
                    moveBundle = batchMoveBundle.moveBundle(args.destination)
                )
                moveResults.add(moveResult)
            }
        }
        moveResults
    }

    @Parcelize
    data class Args(
        val batchMoveBundles: List<BatchMoveBundle>,
        val destination: MoveDestination,
    ) : Parcelable {
        companion object {
            const val EXTRA = "com.w2sv.navigator.extra.BatchMoveBroadcastReceiver.Args"
        }
    }

    companion object {
        fun sendBroadcast(
            args: Args,
            context: Context,
        ) {
            context.sendBroadcast(
                getIntent(
                    args = args,
                    context = context
                )
            )
        }

        private const val ACTION_CANCEL_BATCH_MOVE =
            "com.w2sv.filenavigator.action.CANCEL_BATCH_MOVE"

        fun cancelBatchMoveIntent(context: Context): Intent =
            Intent(context, BatchMoveBroadcastReceiver::class.java)
                .setAction(ACTION_CANCEL_BATCH_MOVE)

        fun getIntent(
            args: Args,
            context: Context
        ): Intent =
            Intent(context, BatchMoveBroadcastReceiver::class.java)
                .putExtra(Args.EXTRA, args)
    }
}