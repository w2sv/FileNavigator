package com.w2sv.navigator.system_broadcastreceiver.manager

import android.content.Context
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.collectFromFlow
import com.w2sv.navigator.system_broadcastreceiver.BootCompletedReceiver
import com.w2sv.navigator.system_broadcastreceiver.PowerSaveModeChangedReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import slimber.log.i
import javax.inject.Inject

internal class NavigatorConfigControlledSystemBroadcastReceiverManagerImpl @Inject constructor(
    navigatorConfigDataSource: NavigatorConfigDataSource,
    private val bootCompletedReceiver: BootCompletedReceiver,
    private val powerSaveModeChangedReceiver: PowerSaveModeChangedReceiver
) : NavigatorConfigControlledSystemBroadcastReceiverManager {

    override fun toggleReceiversOnStatusChange(collectionScope: CoroutineScope, context: Context) {
        with(collectionScope) {
            collectFromFlow(disabledOnLowBatteryDistinctUntilChanged) {
                i { "Collected disableOnLowBattery=$it" }
                powerSaveModeChangedReceiver.toggle(it, context)
            }
            collectFromFlow(startOnBootDistinctUntilChanged) {
                i { "Collected startOnBootCompleted=$it" }
                bootCompletedReceiver.toggle(it, context)
            }
        }
    }

    private val disabledOnLowBatteryDistinctUntilChanged =
        navigatorConfigDataSource.navigatorConfig.map { it.disableOnLowBattery }
            .distinctUntilChanged()

    private val startOnBootDistinctUntilChanged =
        navigatorConfigDataSource.navigatorConfig.map { it.startOnBoot }
            .distinctUntilChanged()
}