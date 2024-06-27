package com.w2sv.navigator.system_action_broadcastreceiver

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.w2sv.androidutils.isServiceRunning
import com.w2sv.navigator.FileNavigator
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

internal class BootCompletedReceiverTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun testOnReceive() {
        val receiver = BootCompletedReceiver()
        receiver.register(context)
        executeAdbCommand()
        assertTrue(context.isServiceRunning<FileNavigator>())
    }
}

fun executeAdbCommand() {
    val adbCommand = arrayOf(
        "adb",
        "shell",
        "su",
        "root",
        "am",
        "broadcast",
        "-a",
        "android.intent.action.BOOT_COMPLETED"
    )

    val processBuilder = ProcessBuilder(*adbCommand)
    processBuilder.redirectErrorStream(true)

    try {
        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val output = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            output.append(line).append("\n")
        }

        process.waitFor()

        println("ADB Command Output: $output")
        assert(process.exitValue() == 0) { "ADB command failed" }
    } catch (e: Exception) {
        e.printStackTrace()
        assert(false) { "Exception while executing ADB command: ${e.message}" }
    }
}