package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.notifications.NotificationResources
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
internal class MoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    internal lateinit var moveResultListener: MoveResultListener

    override fun onReceive(context: Context, intent: Intent) {
        val args = intent.getParcelableCompat<Args>(Args.EXTRA)!!

        moveResultListener.invoke(
            moveResult = args.moveBundle.move(context),
            notificationResources = args.notificationResources
        )
    }

    @Parcelize
    data class Args(
        val moveBundle: MoveBundle,
        val notificationResources: NotificationResources? = null
    ) : Parcelable {
        companion object {
            const val EXTRA = "com.w2sv.navigator.extra.MoveBroadcastReceiver.Args"
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
            Intent(context, MoveBroadcastReceiver::class.java)
                .putExtra(Args.EXTRA, args)
    }
}