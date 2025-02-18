package com.w2sv.navigator.moving.batch

import android.content.Context
import android.content.Intent
import com.w2sv.common.util.LoggingBroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class CancelBatchMoveBroadcastReceiver : LoggingBroadcastReceiver() {

    @Inject
    lateinit var batchMoveJobHolder: BatchMoveBroadcastReceiver.JobHolder

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        batchMoveJobHolder.job?.cancel()
    }

    companion object {
        fun intent(context: Context): Intent =
            Intent(context, CancelBatchMoveBroadcastReceiver::class.java)
    }
}
