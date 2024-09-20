package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.MoveDestination
import com.w2sv.navigator.MoveResultChannel
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.managers.BatchMoveProgressNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
internal class BatchMoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var moveResultChannel: MoveResultChannel

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

        val destinationDocumentFile = args.destination.documentFile(context)!!

        jobHolder.job = scope.launch {
            batchMoveProgressNotificationManager.buildAndPostNotification(
                BatchMoveProgressNotificationManager.BuilderArgs.MoveProgress(
                    current = 0,
                    max = args.batchMoveBundles.size
                )
            )
            val moveResults = mutableListOf<MoveResult>()
            try {
                args.batchMoveBundles.forEachIndexed { index, moveBundle ->
                    if (isActive) {
                        moveBundle.file.moveTo(
                            destination = args.destination,
                            destinationDocumentFile = destinationDocumentFile,
                            context = context
                        ) { result ->
                            batchMoveProgressNotificationManager.buildAndPostNotification(
                                BatchMoveProgressNotificationManager.BuilderArgs.MoveProgress(
                                    current = index + 1,
                                    max = args.batchMoveBundles.size
                                )
                            )
                            moveResultChannel.trySend(result bundleWith moveBundle)
                            moveResults.add(result)
                        }
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

    @Parcelize
    data class Args(
        val batchMoveBundles: List<MoveBundle.Batchable<*>>,
    ) : Parcelable {

        val destination: MoveDestination.Directory
            get() = batchMoveBundles.first().destination

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