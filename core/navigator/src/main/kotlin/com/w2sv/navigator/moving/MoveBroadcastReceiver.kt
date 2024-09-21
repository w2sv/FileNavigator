package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.domain.model.MoveDestination
import com.w2sv.navigator.MoveResultChannel
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.DestinationSelectionManner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class MoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var moveResultChannel: MoveResultChannel

    @Inject
    @GlobalScope(AppDispatcher.IO)
    lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val moveBundle = MoveBundle.fromIntent<MoveDestination, DestinationSelectionManner>(intent)

        scope.launch {
            with(moveBundle) {
                file.moveTo(destination = destination, context = context) { result ->
                    moveResultChannel.trySend(
                        result bundleWith this
                    )
                }
            }
        }
    }

    companion object {
        fun sendBroadcast(
            moveBundle: MoveBundle<*, *>,
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
            moveBundle: MoveBundle<*, *>,
            context: Context
        ): Intent =
            Intent(context, MoveBroadcastReceiver::class.java)
                .putExtra(MoveBundle.EXTRA, moveBundle)
    }
}