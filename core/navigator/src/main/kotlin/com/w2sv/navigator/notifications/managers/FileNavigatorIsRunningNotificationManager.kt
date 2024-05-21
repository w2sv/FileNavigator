package com.w2sv.navigator.notifications.managers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.w2sv.core.navigator.R
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.getNotificationChannel
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FileNavigatorIsRunningNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager
) : AppNotificationManager<AppNotificationManager.BuilderArgs.Empty>(
    notificationChannel = AppNotificationChannel.FileNavigatorIsRunning.getNotificationChannel(
        context
    ),
    notificationManager = notificationManager,
    context = context
) {
    override fun getBuilder(args: BuilderArgs.Empty): Builder =
        object : Builder() {
            override fun build(): Notification {
                setContentTitle(context.getString(R.string.file_navigator_is_running))

                setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            context.getString(R.string.you_will_receive_a_notification_when_a_new_file_corresponding_to_your_selected_file_types_enters_the_file_system)
                        )
                )

                foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE

                setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        1,
                        Intent.makeRestartActivityTask(
                            ComponentName(context, "com.w2sv.filenavigator.MainActivity")
                        ),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )

                // add stop action
                addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_cancel_24,
                        context.getString(R.string.stop),
                        PendingIntent.getService(
                            context,
                            0,
                            FileNavigator.getStopIntent(context),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                        ),
                    )
                )

                return super.build()
            }
        }
}