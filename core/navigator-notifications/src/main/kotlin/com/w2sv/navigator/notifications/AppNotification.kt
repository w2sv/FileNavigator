package com.w2sv.navigator.notifications

/**
 * Enum assures required [id]-uniqueness.
 */
internal enum class AppNotification(val channel: AppNotificationChannel) {
    FileNavigatorIsRunning(AppNotificationChannel.FileNavigatorIsRunning),
    NewNavigatableFile(AppNotificationChannel.NewNavigatableFile),
    AutoMoveDestinationInvalid(AppNotificationChannel.AutoMoveDestinationInvalid),
    BatchMoveFiles(AppNotificationChannel.MoveProgress),
    BatchMoveProgress(AppNotificationChannel.MoveProgress);

    val id: Int get() = ordinal + 1 // 0 is an invalid notification ID
    val multiInstanceIdBase: Int get() = id * 1000
    val summaryId: Int get() = multiInstanceIdBase + 999
}
