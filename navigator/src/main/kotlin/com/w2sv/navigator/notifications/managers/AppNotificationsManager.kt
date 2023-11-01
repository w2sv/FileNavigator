package com.w2sv.navigator.notifications.managers

import android.app.NotificationManager
import android.content.Context
import com.w2sv.navigator.notifications.managers.newmovefile.NewMoveFileNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for manipulating, i.e. showing and cancelling, app notifications.
 */
@Singleton
class AppNotificationsManager @Inject constructor(
    notificationManager: NotificationManager,
    @ApplicationContext context: Context
) {
    val newMoveFileNotificationManager = NewMoveFileNotificationManager(
        context = context,
        notificationManager = notificationManager
    )

    val fileNavigatorIsRunningNotificationManager = FileNavigatorIsRunningNotificationManager(
        context = context,
        notificationManager = notificationManager
    )
}