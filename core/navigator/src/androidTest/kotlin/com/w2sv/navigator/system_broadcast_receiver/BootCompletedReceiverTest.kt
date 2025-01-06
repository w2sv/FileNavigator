package com.w2sv.navigator.system_broadcast_receiver

import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.w2sv.common.util.log
import com.w2sv.navigator.system_broadcastreceiver.BootCompletedReceiver
import org.junit.Before
import org.junit.Test

class BootCompletedReceiverTest {

    private val receiver = BootCompletedReceiver()
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun test() {
        receiver.toggle(true, ApplicationProvider.getApplicationContext())
        device.executeShellCommand("adb root").log()
    }
}