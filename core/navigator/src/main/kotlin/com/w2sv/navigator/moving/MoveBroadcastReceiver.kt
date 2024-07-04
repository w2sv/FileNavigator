package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.shared.putMoveBundleExtra
import com.w2sv.navigator.shared.putOptionalNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class MoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    internal lateinit var fileMover: FileMover

    @Inject
    internal lateinit var moveResultListener: MoveResultListener

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val moveResult = fileMover.invoke(
            moveBundle = MoveBundle.fromIntent(intent),
            context = context
        )
        moveResultListener.invoke(
            moveResult = moveResult,
            notificationResources = NotificationResources.fromIntent(intent)
        )
    }

    companion object {
        fun sendBroadcast(
            context: Context,
            moveBundle: MoveBundle,
            notificationResources: NotificationResources? = null
        ) {
            context.sendBroadcast(
                getIntent(
                    moveBundle = moveBundle,
                    notificationResources = notificationResources,
                    context = context
                )
            )
        }

        fun getIntent(
            moveBundle: MoveBundle,
            notificationResources: NotificationResources?,
            context: Context
        ): Intent =
            Intent(context, MoveBroadcastReceiver::class.java)
                .putMoveBundleExtra(moveBundle)
                .putOptionalNotificationResourcesExtra(notificationResources)
    }
}