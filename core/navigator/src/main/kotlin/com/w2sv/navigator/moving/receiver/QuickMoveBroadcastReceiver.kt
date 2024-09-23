package com.w2sv.navigator.moving.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.navigator.moving.activity.QuickMoveDestinationPermissionQueryActivity
import com.w2sv.navigator.moving.model.MoveBundle

internal class QuickMoveBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val moveBundle = MoveBundle.fromIntent<MoveBundle.QuickMove>(intent)

        if (moveBundle.destination.documentFile(context).name == null) {
            QuickMoveDestinationPermissionQueryActivity.start(moveBundle, context)
        } else {
            MoveBroadcastReceiver.sendBroadcast(
                moveBundle = moveBundle,
                context = context
            )
        }
    }

    companion object {
        fun getIntent(
            moveBundle: MoveBundle.QuickMove,
            context: Context
        ): Intent =
            Intent(context, QuickMoveBroadcastReceiver::class.java)
                .putExtra(MoveBundle.EXTRA, moveBundle)
    }
}