package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.utils.DocumentUri
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.notifications.NotificationResources
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
                moveResultListener.invoke(preMoveCheckResult.failure)
            }

            is PreMoveCheckResult.Success -> {
                batchMoveProgressNotificationManager.buildAndPost(
                    current = 0,
                    max = args.moveFiles.size
                )
                args.moveFiles.zip(args.notificationResources).withIndex().forEach {
                    val (moveFile, notificationResources) = it.value
                    val moveResult = moveFile.moveTo(
                        preMoveCheckResult.documentFile,
                        context,
                        makeMoveBundle = {
                            MoveBundle(
                                file = moveFile,
                                destination = args.destination,
                                mode = MoveMode.Quick
                            )
                        }
                    )
                    batchMoveProgressNotificationManager.buildAndPost(
                        current = it.index + 1,
                        max = args.moveFiles.size
                    )
                    moveResultListener.invoke(  // TODO
                        moveResult = moveResult,
                        notificationResources = notificationResources
                    )
                }
            }
        }
    }

    @Parcelize
    data class Args(
        val moveFiles: List<MoveFile>,
        val destination: DocumentUri,
        val notificationResources: List<NotificationResources>
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