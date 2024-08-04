package com.w2sv.navigator.moving

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class CancelBatchMoveBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var jobHolder: BatchMoveBroadcastReceiver.JobHolder

    override fun onReceive(context: Context, intent: Intent) {
        jobHolder.job?.cancel()
    }

    companion object {
        fun intent(context: Context): Intent =
            Intent(context, CancelBatchMoveBroadcastReceiver::class.java)
    }
}