package com.w2sv.navigator.notifications

import android.content.Intent
import com.w2sv.navigator.model.MoveFile

internal fun Intent.putNavigatableFileExtra(moveFile: MoveFile): Intent =
    putExtra(MoveFile.EXTRA, moveFile)

internal fun Intent.putNotificationResourcesExtra(notificationResources: NotificationResources): Intent =
    putExtra(
        NotificationResources.EXTRA,
        notificationResources
    )