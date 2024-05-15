package com.w2sv.navigator.notifications

import android.content.Intent
import com.w2sv.navigator.moving.MoveFile

internal fun Intent.putMoveFileExtra(moveFile: MoveFile): Intent =
    putExtra(MoveFile.EXTRA, moveFile)

internal fun Intent.putNotificationResourcesExtra(notificationResources: NotificationResources): Intent =
    putExtra(
        NotificationResources.EXTRA,
        notificationResources
    )