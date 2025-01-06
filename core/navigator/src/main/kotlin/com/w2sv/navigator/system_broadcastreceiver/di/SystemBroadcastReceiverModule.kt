package com.w2sv.navigator.system_broadcastreceiver.di

import com.w2sv.navigator.system_broadcastreceiver.BootCompletedReceiver
import com.w2sv.navigator.system_broadcastreceiver.PowerSaveModeChangedReceiver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal object SystemBroadcastReceiverModule {

    @Provides
    fun bootCompletedReceiver(): BootCompletedReceiver =
        BootCompletedReceiver()

    @Provides
    fun powerSaveModeChangedReceiver(): PowerSaveModeChangedReceiver =
        PowerSaveModeChangedReceiver()
}
