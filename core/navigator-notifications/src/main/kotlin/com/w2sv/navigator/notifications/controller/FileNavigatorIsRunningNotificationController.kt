package com.w2sv.navigator.notifications.controller

import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.w2sv.core.common.R
import com.w2sv.navigator.domain.NavigatorIntents
import com.w2sv.navigator.notifications.AppNotification
import com.w2sv.navigator.notifications.api.NotificationEnvironment
import com.w2sv.navigator.notifications.api.SingleNotificationController
import com.w2sv.navigator.notifications.api.setBigTextStyle
import javax.inject.Inject

internal typealias EmptyArgs = Unit

internal class FileNavigatorIsRunningNotificationController @Inject constructor(
    environment: NotificationEnvironment,
    private val navigatorIntents: NavigatorIntents
) : SingleNotificationController<EmptyArgs>(
    environment = environment,
    appNotification = AppNotification.FileNavigatorIsRunning
) {
    override fun NotificationCompat.Builder.configure(args: EmptyArgs, id: Int) {
        setContentTitle(context.getString(R.string.file_navigator_is_running))
        setBigTextStyle(context.getString(R.string.foreground_service_notification_content))

        foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE

        setContentIntent(navigatorIntents.openMainActivity())
        addAction(stopNavigatorAction())
    }

    private fun stopNavigatorAction(): NotificationCompat.Action =
        NotificationCompat.Action(
            R.drawable.ic_cancel_24,
            context.getString(R.string.stop),
            PendingIntent.getService(
                context,
                0,
                navigatorIntents.stopNavigator(),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )
        )
}
