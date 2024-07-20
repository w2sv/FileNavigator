package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.domain.model.MoveDestination
import com.w2sv.navigator.moving.model.BatchMoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.managers.BatchMoveProgressNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
internal class BatchMoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var moveResultListener: MoveResultListener

    @Inject
    lateinit var batchMoveProgressNotificationManager: BatchMoveProgressNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val args = intent.getParcelableCompat<Args>(Args.EXTRA)!!

        when (val preMoveCheckResult = sharedPreMoveChecks(args.destination, context)) {
            is PreMoveCheckResult.Failure -> {
                moveResultListener.onPreMoveCancellation(preMoveCheckResult.failure, null)
            }

            is PreMoveCheckResult.Success -> {
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

    private fun batchMove(
        args: Args,
        destinationDocumentFile: DocumentFile,
        context: Context
    ): List<MoveResult> {
        val moveResults = mutableListOf<MoveResult>()
        batchMoveProgressNotificationManager.buildAndPostNotification(
            BatchMoveProgressNotificationManager.BuilderArgs.MoveProgress(
                current = 0,
                max = args.batchMoveBundles.size
            )
        )
        args.batchMoveBundles.forEachIndexed { index, batchMoveBundle ->
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
        return moveResults
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

        private fun getIntent(
            args: Args,
            context: Context
        ): Intent =
            Intent(context, BatchMoveBroadcastReceiver::class.java)
                .putExtra(Args.EXTRA, args)
    }
}