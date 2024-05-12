package com.w2sv.navigator.notifications

import android.content.Intent
import android.net.Uri
import com.w2sv.navigator.model.MoveFile
import com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.receivers.MoveBroadcastReceiver

internal fun Intent.putMoveFileExtra(moveFile: MoveFile): Intent =
    putExtra(MoveFile.EXTRA, moveFile)

internal fun Intent.putNotificationResourcesExtra(notificationResources: NotificationResources): Intent =
    putExtra(
        NotificationResources.EXTRA,
        notificationResources
    )

internal fun Intent.putMoveDestinationExtra(moveDestination: Uri): Intent =
    putExtra(
        MoveBroadcastReceiver.EXTRA_MOVE_DESTINATION,
        moveDestination
    )