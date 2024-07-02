package com.w2sv.navigator.notifications

import android.content.Intent
import com.w2sv.navigator.moving.MoveBundle

internal fun Intent.putMoveFileExtra(moveBundle: MoveBundle): Intent =
    putExtra(MoveBundle.EXTRA, moveBundle)

internal fun Intent.putOptionalNotificationResourcesExtra(notificationResources: NotificationResources?): Intent =
    putExtra(
        NotificationResources.EXTRA,
        notificationResources
    )