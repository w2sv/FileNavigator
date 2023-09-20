package com.w2sv.navigator.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.R

class ForegroundServiceNotificationManager(
    private val context: Context,
    notificationManager: NotificationManager
): AppNotificationManager(
    notificationChannel = getNotificationChannel(
        id = "FILE_NAVIGATOR",
        name = context.getString(R.string.file_navigator_is_running)
    ),
    notificationManager = notificationManager,
    resourcesBaseSeed = -1
) {

    fun build(): Notification {
        return Builder().build()
    }

    inner class Builder : NotificationCompat.Builder(context, notificationChannel.id) {
        override fun build(): Notification {
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.ic_file_move_24)
            setContentTitle(context.getString(R.string.file_navigator_is_running))

            // add configure action
            addAction(
                NotificationCompat.Action(
                    R.drawable.ic_settings_24,
                    context.getString(R.string.configure),
                    PendingIntent.getActivity(
                        context,
                        1,
                        Intent.makeRestartActivityTask(
                            ComponentName(context, "com.w2sv.filenavigator.MainActivity")
                        ),
                        PendingIntent.FLAG_IMMUTABLE
                    )
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