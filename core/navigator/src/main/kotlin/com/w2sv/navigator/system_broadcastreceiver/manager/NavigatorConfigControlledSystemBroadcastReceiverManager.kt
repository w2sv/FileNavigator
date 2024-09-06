package com.w2sv.navigator.system_broadcastreceiver.manager

import android.content.Context
import kotlinx.coroutines.CoroutineScope

interface NavigatorConfigControlledSystemBroadcastReceiverManager {
    fun toggleReceiversOnStatusChange(collectionScope: CoroutineScope, context: Context)
}

