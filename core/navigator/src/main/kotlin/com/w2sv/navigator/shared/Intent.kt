package com.w2sv.navigator.shared

import android.content.Intent
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.notifications.NotificationResources

internal fun Intent.putMoveFileExtra(moveFile: MoveFile): Intent =
    putExtra(MoveFile.EXTRA, moveFile)

internal fun Intent.putMoveBundleExtra(moveBundle: MoveBundle): Intent =
    putExtra(MoveBundle.EXTRA, moveBundle)

internal fun Intent.putOptionalNotificationResourcesExtra(notificationResources: NotificationResources?): Intent =
    putExtra(
        NotificationResources.EXTRA,
        notificationResources
    )