package com.w2sv.navigator.shared

import android.content.Intent
import com.w2sv.navigator.notifications.NotificationResources

internal fun Intent.putOptionalNotificationResourcesExtra(notificationResources: NotificationResources?): Intent =
    putExtra(
        NotificationResources.EXTRA,
        notificationResources
    )