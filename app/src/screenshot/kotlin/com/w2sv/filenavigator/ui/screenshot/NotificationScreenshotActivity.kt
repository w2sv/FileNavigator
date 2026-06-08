package com.w2sv.filenavigator.ui.screenshot

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.NotificationManagerCompat
import com.w2sv.navigator.domain.notifications.ForegroundNotificationProvider
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationScreenshotActivity : ComponentActivity() {

    @Inject
    lateinit var notificationEventHandler: NotificationEventHandler

    @Inject
    lateinit var foregroundNotificationProvider: ForegroundNotificationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fixture = StoreScreenshotFixture(this)
        postFixtureNotifications(fixture)

        setContent {
            StoreScreenshotTheme(fixture) {
                StoreScreenshotContent(
                    screenshot = StoreScreenshot.HOME,
                    fixture = fixture
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun postFixtureNotifications(fixture: StoreScreenshotFixture) {
        // StoreScreenshotTest grants POST_NOTIFICATIONS before this screenshot-only activity is launched.
        // The running notification bypasses NotificationEventHandler in production and is posted by the foreground service.
        NotificationManagerCompat.from(this).notify(
            foregroundNotificationProvider.notificationId,
            foregroundNotificationProvider.notification()
        )

        fixture.notifications.forEach {
            notificationEventHandler(NotificationEvent.PostNavigateFile(it))
        }
    }
}
