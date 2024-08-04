package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.navigator.moving.model.MoveBundle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class MoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    internal lateinit var moveResultListener: MoveResultListener

    override fun onReceive(context: Context, intent: Intent) {
        val moveBundle = MoveBundle.fromIntent(intent)

        moveResultListener.invoke(
            moveBundle = moveBundle,
            moveResult = moveBundle.move(context),
        )
    }

    companion object {
        fun sendBroadcast(
            moveBundle: MoveBundle,
            context: Context,
        ) {
            context.sendBroadcast(
                getIntent(
                    moveBundle = moveBundle,
                    context = context
                )
            )
        }

        fun getIntent(
            moveBundle: MoveBundle,
            context: Context
        ): Intent =
            Intent(context, MoveBroadcastReceiver::class.java)
                .putExtra(MoveBundle.EXTRA, moveBundle)
    }
}