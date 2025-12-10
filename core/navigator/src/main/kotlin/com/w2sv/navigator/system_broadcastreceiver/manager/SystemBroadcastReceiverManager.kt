package com.w2sv.navigator.system_broadcastreceiver.manager

import android.content.Context
import kotlinx.coroutines.CoroutineScope

interface SystemBroadcastReceiverManager {
    fun launchReceiverTogglingOnNavigatorConfigChange(flowCollectionScope: CoroutineScope, context: Context)
}
