//package com.w2sv.navigator.quicktile
//
//import android.app.Instrumentation
//import android.content.Context
//import android.os.ParcelFileDescriptor
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
//import androidx.test.uiautomator.By
//import androidx.test.uiautomator.UiDevice
//import androidx.test.uiautomator.UiObject
//import androidx.test.uiautomator.UiSelector
//import androidx.test.uiautomator.Until
//import app.cash.turbine.test
//import com.w2sv.navigator.FileNavigator
//import dagger.hilt.android.testing.HiltAndroidRule
//import dagger.hilt.android.testing.HiltAndroidTest
//import kotlinx.coroutines.test.runTest
//import org.junit.Assert.assertFalse
//import org.junit.Assert.assertTrue
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import java.io.FileInputStream
//import java.io.IOException
//import javax.inject.Inject
//import kotlin.time.Duration.Companion.seconds
//
//@HiltAndroidTest
//class FileNavigatorTileServiceTest {
//
//    @get:Rule
//    val hiltRule = HiltAndroidRule(this)
//
//    @Inject
//    lateinit var fileNavigatorIsRunning: FileNavigator.IsRunning
//
//    private lateinit var device: UiDevice
//
//    @Before
//    fun setUp() {
//        device = UiDevice.getInstance(getInstrumentation())
//        hiltRule.inject()
//        FileNavigator.stop(context)
//    }
//
//    private val context: Context = ApplicationProvider.getApplicationContext()
//    private val tileLabel = context.getString(com.w2sv.core.common.R.string.app_name)
//
//    @Test
//    fun testTileService() = runTest {
//        fileNavigatorIsRunning.test(timeout = 5.seconds) {
//            assertFalse(awaitItem())
//
//            device.openQuickSettings()
//            device.wait(Until.hasObject(By.textContains(tileLabel)), 2_000)
//
//            val tile = device.findObject(By.textContains(tileLabel))
//            requireNotNull(tile)
//
//            tile.click()
//            assertTrue(awaitItem())
//
//            // Click the tile again to toggle its state
//            device.openQuickSettings()
//            device.wait(Until.hasObject(By.textContains(tileLabel)), 2_000)
//
//            tile.click()
//            assertFalse(awaitItem())
//        }
//    }
//}
//
//private fun UiDevice.findObjects(selector: UiSelector): List<UiObject> {
//    val objects = mutableListOf<UiObject>()
//    var index = 0
//    while (true) {
//        val obj = findObject(selector.instance(index))
//        if (obj.exists()) {
//            objects.add(obj)
//            index++
//        } else {
//            break
//        }
//    }
//    return objects
//}
//
///**
// * Executes a shell command using shell user identity, and return the standard output in string
// *
// * Note: calling this function requires API level 21 or above
// * @param instrumentation [Instrumentation] instance, obtained from a test running in
// * instrumentation framework
// * @param cmd the command to run
// * @return the standard output of the command
// * @throws Exception
// */
//@Throws(IOException::class)
//private fun runShellCommand(instrumentation: Instrumentation, cmd: String?): String {
//    val pfd = instrumentation.uiAutomation.executeShellCommand(cmd)
//    val buf = ByteArray(512)
//    var bytesRead: Int
//    val fis: FileInputStream = ParcelFileDescriptor.AutoCloseInputStream(pfd)
//    val stdout = StringBuffer()
//    while ((fis.read(buf).also { bytesRead = it }) != -1) {
//        stdout.append(String(buf, 0, bytesRead))
//    }
//    fis.close()
//    return stdout.toString()
//}