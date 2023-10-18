package com.w2sv.navigator.notifications

import android.content.Intent
import com.w2sv.navigator.model.NavigatableFile

internal fun Intent.putNavigatableFileExtra(navigatableFile: NavigatableFile): Intent =
    putExtra(NavigatableFile.EXTRA, navigatableFile)

internal fun Intent.putNotificationResourcesExtra(notificationResources: NotificationResources): Intent =
    putExtra(
        NotificationResources.EXTRA,
        notificationResources
    )