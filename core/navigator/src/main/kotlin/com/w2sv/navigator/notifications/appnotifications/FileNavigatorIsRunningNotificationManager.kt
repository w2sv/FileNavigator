package com.w2sv.navigator.notifications.appnotifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.w2sv.core.common.R
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.notifications.api.AppNotificationManager
import com.w2sv.navigator.shared.mainActivityPendingIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class FileNavigatorIsRunningNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager
) : AppNotificationManager<Unit>(
    appNotificationChannel = AppNotificationChannel.FileNavigatorIsRunning,
    notificationManager = notificationManager,
    context = context
) {
    override fun getBuilder(args: Unit): Builder =
        object : Builder() {
            override fun build(): Notification {
                setContentTitle(context.getString(R.string.file_navigator_is_running))

                setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            context.getString(R.string.foreground_service_notification_content)
                        )
                )

                foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE

                setContentIntent(
                    mainActivityPendingIntent(context)
                )

                // add stop action
                addAction(
                    NotificationCompat.Action(
                        com.w2sv.core.common.R.drawable.ic_cancel_24,
                        context.getString(R.string.stop),
                        PendingIntent.getService(
                            context,
                            0,
                            FileNavigator.getStopIntent(context),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                        )
                    )
                )

                return super.build()
            }
        }
}
