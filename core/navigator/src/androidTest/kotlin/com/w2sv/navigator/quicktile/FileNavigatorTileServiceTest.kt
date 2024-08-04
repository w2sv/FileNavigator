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
//import com.w2sv.navigator.FileNavigator
//import dagger.hilt.android.testing.HiltAndroidRule
//import dagger.hilt.android.testing.HiltAndroidTest
//import org.junit.Assert.assertFalse
//import org.junit.Assert.assertTrue
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import java.io.FileInputStream
//import java.io.IOException
//import javax.inject.Inject
//
//@HiltAndroidTest
//internal class FileNavigatorTileServiceTest {
//
//    @get:Rule
//    var hiltRule = HiltAndroidRule(this)
//
//    @Inject
//    lateinit var fileNavigatorIsRunning: FileNavigator.IsRunning
//
//    private lateinit var device: UiDevice
//    private val context: Context = ApplicationProvider.getApplicationContext()
//
//    private val tileLabel = context.getString(com.w2sv.core.common.R.string.app_name)
////    private val tileService = FileNavigatorTileService()
//
//    @Before
//    fun setUp() {
//        hiltRule.inject()
//        device = UiDevice.getInstance(getInstrumentation())
//        FileNavigator.stop(context)
//    }
//
//    @Test
//    fun testTileService() {
//        val tileService = FileNavigatorTileService()
//        println(tileService.scope.coroutineContext)
//        assertFalse(tileService.fileNavigatorIsRunning.value)
//        device.openQuickSettings()
//        device.waitForIdle()
//
//        device.wait(Until.hasObject(By.textContains(tileLabel)), 10_000)
//        val tile = device.findObject(By.textContains(tileLabel).clickable(true))
//        assert(tile != null)
//
//        tile.click()
//        device.wait({ false }, 5_000)
//        assertTrue(fileNavigatorIsRunning.value)
////        assertNotNull(tileService.qsTile)
////        assertEquals(Tile.STATE_ACTIVE, tileService.qsTile.state)
//
//        // Click the tile again to toggle its state
//        tile.click()
//        device.waitForIdle()
//        assertFalse(fileNavigatorIsRunning.value)
//
//        // Verify the tile state changed to inactive
////        assertEquals(Tile.STATE_INACTIVE, tileService.qsTile.state)
//    }
//
//    private fun UiDevice.findObjects(selector: UiSelector): List<UiObject> {
//        val objects = mutableListOf<UiObject>()
//        var index = 0
//        while (true) {
//            val obj = findObject(selector.instance(index))
//            if (obj.exists()) {
//                objects.add(obj)
//                index++
//            } else {
//                break
//            }
//        }
//        return objects
//    }
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
//fun runShellCommand(instrumentation: Instrumentation, cmd: String?): String {
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