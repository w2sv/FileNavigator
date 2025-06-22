package com.w2sv.navigator.system_broadcastreceiver.manager

import android.content.Context
import com.w2sv.common.util.logIdentifier
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.navigator.system_broadcastreceiver.BootCompletedReceiver
import com.w2sv.navigator.system_broadcastreceiver.PowerSaveModeChangedReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import slimber.log.i
import javax.inject.Inject

internal class NavigatorConfigControlledSystemBroadcastReceiverManagerImpl @Inject constructor(
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val bootCompletedReceiver: BootCompletedReceiver,
    private val powerSaveModeChangedReceiver: PowerSaveModeChangedReceiver
) : NavigatorConfigControlledSystemBroadcastReceiverManager {

    override fun toggleReceiversOnStatusChange(collectionScope: CoroutineScope, context: Context) {
        val flowToReceiver = mapOf(
            navigatorConfigDataSource.navigatorConfig.map { it.disableOnLowBattery } to powerSaveModeChangedReceiver,
            navigatorConfigDataSource.navigatorConfig.map { it.startOnBoot } to bootCompletedReceiver
        )
        flowToReceiver.forEach { (flow, receiver) ->
            flow.distinctUntilChanged().collectOn(collectionScope, Dispatchers.IO) { register ->
                i { "Toggling ${receiver.logIdentifier} to $register based on respective control flow emission" }
                receiver.toggle(register, context)
            }
        }
    }
}
