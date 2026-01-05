package com.w2sv.navigator.domain.notifications

import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveFile
import com.w2sv.navigator.domain.moving.MoveResult
import kotlinx.parcelize.Parcelize

sealed interface NotificationEvent {

    @JvmInline
    value class PostMoveFile(val moveFile: MoveFile) : NotificationEvent

    @Parcelize
    @JvmInline
    value class CancelMoveFile(val id: Int) : CancelNotificationEvent

    data class AutoMoveDestinationInvalid(val destination: MoveDestination, val fileAndSourceType: FileAndSourceType) :
        NotificationEvent

    @Parcelize
    @JvmInline
    value class CancelAutoMoveDestinationInvalid(val id: Int) : CancelNotificationEvent

    data class BatchMoveProgress(val current: Int, val total: Int) : NotificationEvent
    data class BatchMoveResults(val moveResults: List<MoveResult>, val destination: MoveDestination.Directory) : NotificationEvent
}
