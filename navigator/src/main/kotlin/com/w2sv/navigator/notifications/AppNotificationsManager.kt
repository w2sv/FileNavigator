package com.w2sv.navigator.notifications

import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotificationsManager @Inject constructor(
    notificationManager: NotificationManager,
    @ApplicationContext private val context: Context
) {
    val newMoveFileNotificationManager = NewMoveFileNotificationManager(
        context = context,
        notificationManager = notificationManager
    )

    val foregroundServiceNotificationManagerProducer = ForegroundServiceNotificationManager(
        context = context,
        notificationManager = notificationManager
    )
}