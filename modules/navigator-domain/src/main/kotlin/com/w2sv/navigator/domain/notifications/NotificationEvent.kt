package com.w2sv.navigator.domain.notifications

import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveResult
import com.w2sv.navigator.domain.moving.NavigatableFile
import kotlinx.parcelize.Parcelize

sealed interface NotificationEvent {

    @JvmInline
    value class PostNavigateFile(val file: NavigatableFile) : NotificationEvent

    @Parcelize
    @JvmInline
    value class CancelNavigateFile(val id: Int) : CancelNotificationEvent

    data class AutoMoveDestinationInvalid(val destination: MoveDestination, val fileAndSourceType: FileAndSourceType) :
        NotificationEvent

    @Parcelize
    @JvmInline
    value class CancelAutoMoveDestinationInvalid(val id: Int) : CancelNotificationEvent

    data class BatchMoveProgress(val current: Int, val total: Int) : NotificationEvent
    data class BatchMoveResults(val moveResults: List<MoveResult>, val destination: MoveDestination.Directory) : NotificationEvent
}
