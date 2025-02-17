package com.w2sv.navigator.notifications.appnotifications

/**
 * Enum assures required [id]-uniqueness.
 */
internal enum class AppNotificationId {
    FileNavigatorIsRunning,
    NewNavigatableFile,
    AutoMoveDestinationInvalid,
    BatchMoveFiles,
    MoveProgress;

    val id: Int by lazy {
        ordinal + 1 // 0 is an invalid notification ID
    }

    val multiInstanceIdBase: Int by lazy {
        id * 1000
    }

    val summaryId: Int by lazy {
        multiInstanceIdBase + 999
    }
}
