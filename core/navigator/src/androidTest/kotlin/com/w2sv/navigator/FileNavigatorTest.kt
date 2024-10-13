package com.w2sv.navigator

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.activityScenarioRule
import app.cash.turbine.test
import com.w2sv.androidutils.isServiceRunning
import com.w2sv.navigator.moving.activity.QuickMoveDestinationPermissionQueryOverlayDialogActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class FileNavigatorTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    /**
     * Random activity used for launching of foreground service.
     */
    @get:Rule(order = 1)
    internal val activityScenario =
        activityScenarioRule<QuickMoveDestinationPermissionQueryOverlayDialogActivity>()

    @Inject
    lateinit var fileNavigatorIsRunning: FileNavigator.IsRunning

    @Before
    fun setUp() {
        hiltRule.inject()
        FileNavigator.stop(context)
    }

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `start starts service and emits true on isRunning while stop stops service and emits false on isRunning`() = runTest {
        fileNavigatorIsRunning.test {
            assertFalse(awaitItem())

            startFileNavigator()
            assertTrue(awaitItem())
            assertTrue(context.isServiceRunning<FileNavigator>())

            FileNavigator.stop(context)
            assertFalse(awaitItem())
            assertFalse(context.isServiceRunning<FileNavigator>())
        }
    }

    private fun startFileNavigator() {
        // Launching of foreground service requires app to be in the foreground in some way
        activityScenario.scenario.onActivity {
            FileNavigator.start(it.applicationContext)
        }
    }
}