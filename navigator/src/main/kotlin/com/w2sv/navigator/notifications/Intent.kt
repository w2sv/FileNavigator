package com.w2sv.navigator.notifications

import android.content.Intent
import android.net.Uri
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.model.MoveFile

internal fun Intent.putMoveFileExtra(moveFile: MoveFile): Intent =
    putExtra(MoveFile.EXTRA, moveFile)

internal fun Intent.putNotificationResourcesExtra(notificationResources: NotificationResources): Intent =
    putExtra(
        NotificationResources.EXTRA,
        notificationResources
    )

internal fun Intent.putDefaultMoveDestinationExtra(defaultMoveDestination: Uri): Intent =
    putExtra(
        FileNavigator.EXTRA_DEFAULT_MOVE_DESTINATION,
        defaultMoveDestination
    )