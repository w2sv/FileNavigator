package com.w2sv.navigator.notifications

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
        ordinal + 1  // 0 is an invalid notification ID
    }
}