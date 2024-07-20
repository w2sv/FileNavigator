package com.w2sv.navigator.moving.model

import android.os.Parcelable
import com.w2sv.navigator.notifications.NotificationResources
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MoveFileWithNotificationResources(
    val moveFile: MoveFile,
    val notificationResources: NotificationResources
) : Parcelable