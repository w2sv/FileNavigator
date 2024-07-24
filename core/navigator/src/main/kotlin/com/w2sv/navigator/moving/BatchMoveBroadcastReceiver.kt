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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
internal class BatchMoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var moveResultListener: MoveResultListener

    @Inject
    lateinit var batchMoveProgressNotificationManager: BatchMoveProgressNotificationManager

    @Singleton
    class JobHolder @Inject constructor() {
        var job: Job? = null
    }

    @Inject
    lateinit var jobHolder: JobHolder

    @Inject
    @GlobalScope(AppDispatcher.Default)
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val args = intent.getParcelableCompat<Args>(Args.EXTRA)!!

        when (val preMoveCheckResult = sharedPreMoveChecks(args.destination, context)) {
            is PreMoveCheckResult.Failure -> {
                moveResultListener.onPreMoveCancellation(preMoveCheckResult.failure, null)
            }

            is PreMoveCheckResult.Success -> {
                jobHolder.job = scope.launch {
                    batchMoveProgressNotificationManager.buildAndPostNotification(
                        BatchMoveProgressNotificationManager.BuilderArgs.MoveProgress(
                            current = 0,
                            max = args.batchMoveBundles.size
                        )
                    )
                    val moveResults = mutableListOf<MoveResult>()
                    try {
                        args.batchMoveBundles.forEachIndexed { index, batchMoveBundle ->
                            if (isActive) {
                                val moveResult = batchMoveBundle.moveFile.moveTo(
                                    destination = preMoveCheckResult.documentFile,
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
                    } finally {
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
    }

    private suspend fun batchMove(
        args: Args,
        destinationDocumentFile: DocumentFile,
        context: Context
    ): Flow<MoveResult> = flow {
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
            emit(moveResult)
        }
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

        fun getIntent(
            args: Args,
            context: Context
        ): Intent =
            Intent(context, BatchMoveBroadcastReceiver::class.java)
                .putExtra(Args.EXTRA, args)
    }
}