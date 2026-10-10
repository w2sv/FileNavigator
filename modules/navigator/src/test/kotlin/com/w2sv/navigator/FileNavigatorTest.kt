package com.w2sv.navigator

import android.app.Application
import android.app.Notification
import android.app.Service
import android.content.Intent
import com.w2sv.androidutils.content.intent
import com.w2sv.navigator.domain.notifications.ForegroundNotificationProvider
import com.w2sv.navigator.observing.FileObserverManager
import com.w2sv.test.TimberTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
internal class FileNavigatorTest {

    @get:Rule
    val timberRule = TimberTestRule()

    private val notificationId = 42
    private lateinit var application: Application
    private lateinit var notification: Notification
    private lateinit var observersRegistered: CompletableDeferred<Unit>
    private lateinit var observersReregistered: CompletableDeferred<Unit>
    private lateinit var fileObserverManager: FileObserverManager
    private lateinit var serviceController: ServiceController<FileNavigator>
    private lateinit var service: FileNavigator

    @Before
    fun setUp() {
        application = RuntimeEnvironment.getApplication()
        notification = Notification()
        observersRegistered = CompletableDeferred()
        observersReregistered = CompletableDeferred()
        fileObserverManager = mockk(relaxed = true) {
            coEvery { registerFileObservers() } coAnswers {
                observersRegistered.complete(Unit)
            }
            coEvery { reregisterFileObservers() } coAnswers {
                observersReregistered.complete(Unit)
            }
        }
        val foregroundNotificationProvider = mockk<ForegroundNotificationProvider>()
        every { foregroundNotificationProvider.notificationId } returns notificationId
        every { foregroundNotificationProvider.notification() } returns notification
        // Attach a service context without onCreate/Hilt injection; these tests exercise command dispatch, not the full lifecycle.
        serviceController = Robolectric.buildService(FileNavigator::class.java)
        service = serviceController.get().apply {
            status = FileNavigator.Status()
            this.foregroundNotificationProvider = foregroundNotificationProvider
            this.fileObserverManager = this@FileNavigatorTest.fileObserverManager
            moveResultCollector = mockk(relaxed = true)
        }
    }

    @After
    fun tearDown() {
        // Even without create(), destroy() runs service cleanup and cancels its Dispatchers.Default scope.
        serviceController.destroy()
    }

    @Test
    fun `null intent restarts foreground service and file observers`() {
        assertEquals(Service.START_STICKY, dispatch(null))
        assertForegroundServiceRunning()
        await(observersRegistered)
        coVerify(exactly = 1) { fileObserverManager.registerFileObservers() }
    }

    @Test
    fun `start action starts foreground service and file observers`() {
        FileNavigator.start(application)

        assertEquals(Service.START_STICKY, dispatch(shadowOf(application).nextStartedService))
        assertForegroundServiceRunning()
        await(observersRegistered)
        coVerify(exactly = 1) { fileObserverManager.registerFileObservers() }
    }

    @Test
    fun `stop action stops foreground service`() {
        FileNavigator.start(application)
        dispatch(shadowOf(application).nextStartedService)
        await(observersRegistered)

        FileNavigator.stop(application)

        assertEquals(Service.START_STICKY, dispatch(shadowOf(application).nextStartedService))
        assertFalse(service.status.isRunning.value)
        assertTrue(shadowOf(service).isForegroundStopped)
        assertTrue(shadowOf(service).isStoppedBySelf)
    }

    @Test
    fun `reregister action initiates file observer registration`() {
        FileNavigator.start(application)
        dispatch(shadowOf(application).nextStartedService)
        await(observersRegistered)
        coVerify(exactly = 1) { fileObserverManager.registerFileObservers() }

        FileNavigator.reregisterFileObservers(application)

        assertEquals(Service.START_STICKY, dispatch(shadowOf(application).nextStartedService))
        await(observersReregistered)
        coVerify(exactly = 1) { fileObserverManager.registerFileObservers() }
        coVerify(exactly = 1) { fileObserverManager.reregisterFileObservers() }
        assertForegroundServiceRunning()
    }

    @Test
    fun `intent without action does nothing`() {
        val intent = intent<FileNavigator>(application)
        assertInvalidIntentDoesNothing(intent)
    }

    @Test
    fun `unknown action does nothing`() {
        val intent = intent<FileNavigator>(application).setAction("unknown")
        assertInvalidIntentDoesNothing(intent)
    }

    private fun assertInvalidIntentDoesNothing(intent: Intent) {
        assertEquals(Service.START_STICKY, dispatch(intent))
        assertFalse(service.status.isRunning.value)
        assertEquals(0, shadowOf(service).lastForegroundNotificationId)
        assertNull(shadowOf(service).lastForegroundNotification)
        coVerify(exactly = 0) { fileObserverManager.registerFileObservers() }
    }

    private fun assertForegroundServiceRunning() {
        assertEquals(notificationId, shadowOf(service).lastForegroundNotificationId)
        assertSame(notification, shadowOf(service).lastForegroundNotification)
        assertTrue(service.status.isRunning.value)
    }

    private fun dispatch(intent: Intent?): Int =
        service.onStartCommand(intent, flags = 0, startId = 1)
}

private fun await(signal: CompletableDeferred<Unit>) {
    // Service work runs on Dispatchers.Default, outside a test scheduler; a deferred signal and runBlocking wait for its completion.
    runBlocking { withTimeout(5_000.milliseconds) { signal.await() } }
}
