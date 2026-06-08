package com.w2sv.filenavigator.ui.screenshot

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.core.content.getSystemService
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.io.PlatformTestStorageRegistry
import org.junit.Test

class StoreScreenshotTest {

    @Test
    fun captureStoreScreenshots() {
        prepareDeviceForCapture()
        capture(StoreScreenshot.HOME, "1.png")
        capture(StoreScreenshot.NAVIGATOR_SETTINGS, "2.png")
        capture(StoreScreenshot.APP_SETTINGS, "3.png")
        if (!isTablet) {
            captureNotifications()
        }
    }

    private val isTablet: Boolean
        get() = smallestScreenWidthDp >= TABLET_MIN_WIDTH_DP

    private val smallestScreenWidthDp: Int
        get() = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .resources
            .configuration
            .smallestScreenWidthDp

    private val screenshotDirectory: String
        get() = when {
            smallestScreenWidthDp >= LARGE_TABLET_MIN_WIDTH_DP -> "large-tablet-screenshots"
            smallestScreenWidthDp >= TABLET_MIN_WIDTH_DP -> "tablet-screenshots"
            else -> "phone-screenshots"
        }

    private fun capture(screen: StoreScreenshot, fileName: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val intent = Intent(
            instrumentation.targetContext,
            InAppScreenshotActivity::class.java
        ).putExtra(InAppScreenshotActivity.EXTRA_SCREEN, screen.name)

        ActivityScenario.launch<InAppScreenshotActivity>(intent).use {
            instrumentation.waitForIdleSync()
            SystemClock.sleep(500)
            assertNoAnrDialog()

            val screenshot = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
            PlatformTestStorageRegistry.getInstance()
                .openOutputFile("$screenshotDirectory/$fileName")
                .use { output ->
                    check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output))
                }
        }
    }

    private fun captureNotifications() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.grantRuntimePermission(
            instrumentation.targetContext.packageName,
            Manifest.permission.POST_NOTIFICATIONS
        )
        instrumentation.targetContext.getSystemService<NotificationManager>()?.cancelAll()

        val intent = Intent(
            instrumentation.targetContext,
            NotificationScreenshotActivity::class.java
        )

        ActivityScenario.launch<NotificationScreenshotActivity>(intent).use {
            instrumentation.waitForIdleSync()
            SystemClock.sleep(1_500)
            check(
                instrumentation.uiAutomation.performGlobalAction(
                    AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
                )
            )
            SystemClock.sleep(1_000)
            // Coordinates target the stable API 35 Pixel 6 shade: expand the app group and its two child notifications.
            instrumentation.uiAutomation.executeShellCommand("input tap 945 625").close()
            SystemClock.sleep(500)
            instrumentation.uiAutomation.executeShellCommand("input tap 945 930").close()
            SystemClock.sleep(300)
            instrumentation.uiAutomation.executeShellCommand("input tap 945 800").close()
            SystemClock.sleep(500)
            saveScreenshot("4.png")
        }
    }

    private fun saveScreenshot(fileName: String) {
        val screenshot = checkNotNull(
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        )
        PlatformTestStorageRegistry.getInstance()
            .openOutputFile("$screenshotDirectory/$fileName")
            .use { output ->
                check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, output))
            }
    }

    private fun prepareDeviceForCapture() {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation

        listOf(
            "window_animation_scale",
            "transition_animation_scale",
            "animator_duration_scale"
        ).forEach { setting ->
            executeShellCommand("settings put global $setting 0")
        }

        // Managed devices start tests immediately after boot; tablet System UI needs time to finish its taskbar setup.
        executeShellCommand("am wait-for-broadcast-idle")
        check(uiAutomation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME))
        uiAutomation.waitForIdle(1_000, 15_000)
        waitForStableSystemUi()
    }

    private fun executeShellCommand(command: String): String {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        return ParcelFileDescriptor.AutoCloseInputStream(uiAutomation.executeShellCommand(command)).use {
            it.bufferedReader().readText()
        }
    }

    private fun assertNoAnrDialog() {
        check(!isAnrDialogVisible) {
            "A system ANR dialog is visible; refusing to create invalid store screenshots."
        }
    }

    private fun waitForStableSystemUi() {
        val deadline = SystemClock.elapsedRealtime() + SYSTEM_UI_SETTLE_TIMEOUT_MS
        var stableProcessIds = ""
        var stableSince = 0L

        while (SystemClock.elapsedRealtime() < deadline) {
            val processIds = listOf(
                executeShellCommand("pidof com.android.systemui").trim(),
                executeShellCommand("pidof com.android.launcher3").trim()
            )
            val currentProcessIds = processIds.joinToString()
            val now = SystemClock.elapsedRealtime()

            if (
                processIds.none(String::isEmpty) &&
                !isAnrDialogVisible &&
                isTabletTaskbarReady
            ) {
                if (currentProcessIds != stableProcessIds) {
                    stableProcessIds = currentProcessIds
                    stableSince = now
                } else if (now - stableSince >= SYSTEM_UI_QUIET_PERIOD_MS) {
                    return
                }
            } else {
                stableProcessIds = ""
                stableSince = 0L
            }
            SystemClock.sleep(SYSTEM_UI_POLL_INTERVAL_MS)
        }
        error("System UI did not remain responsive long enough to capture store screenshots.")
    }

    private val isAnrDialogVisible: Boolean
        get() = InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .rootInActiveWindow
            ?.findAccessibilityNodeInfosByText("isn't responding")
            .orEmpty()
            .isNotEmpty()

    private val isTabletTaskbarReady: Boolean
        get() {
            if (smallestScreenWidthDp < LARGE_TABLET_MIN_WIDTH_DP) {
                return true
            }
            val root = InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow
                ?: return false
            // The full AOSP tablet taskbar exposes its pinned apps; the buttons-only fallback does not.
            return TABLET_TASKBAR_APP_LABELS.any { label ->
                root.findAccessibilityNodeInfosByText(label).isNotEmpty()
            }
        }

    private companion object {
        const val TABLET_MIN_WIDTH_DP = 600
        const val LARGE_TABLET_MIN_WIDTH_DP = 720
        const val SYSTEM_UI_QUIET_PERIOD_MS = 5_000L
        const val SYSTEM_UI_SETTLE_TIMEOUT_MS = 30_000L
        const val SYSTEM_UI_POLL_INTERVAL_MS = 250L
        val TABLET_TASKBAR_APP_LABELS = listOf("Calculator", "Photos", "Camera")
    }
}
