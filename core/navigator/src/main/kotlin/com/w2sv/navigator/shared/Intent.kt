package com.w2sv.navigator.shared

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.notifications.NotificationResources

internal fun mainActivityPendingIntent(context: Context): PendingIntent =
    PendingIntent.getActivity(
        context,
        1,
        mainActivityIntent(context),
        PendingIntent.FLAG_IMMUTABLE
    )

internal fun mainActivityIntent(context: Context): Intent =
    Intent.makeRestartActivityTask(
        ComponentName(context, "com.w2sv.filenavigator.ui.MainActivity")
    )

internal operator fun Intent.plus(notificationResources: NotificationResources?): Intent =
    putExtra(NotificationResources.EXTRA, notificationResources)

internal operator fun Intent.plus(moveFile: MoveFile): Intent =
    putExtra(MoveFile.EXTRA, moveFile)
