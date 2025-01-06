package com.w2sv.navigator.moving.model

import androidx.annotation.StringRes
import com.w2sv.core.navigator.R
import com.w2sv.navigator.notifications.NotificationResources

internal sealed class MoveResult(val cancelMoveFileNotification: Boolean) {

    data object Success : MoveResult(true)

    sealed class Failure(
        cancelMoveFileNotification: Boolean,
        @StringRes val explanationStringRes: Int?
    ) : MoveResult(cancelMoveFileNotification) {

        infix fun bundleWith(notificationResources: NotificationResources?): Bundle.PreCheckFailure =
            Bundle.PreCheckFailure(this, notificationResources)
    }

    data object ManageAllFilesPermissionMissing :
        Failure(
            explanationStringRes = R.string.manage_all_files_permission_missing,
            cancelMoveFileNotification = false
        )

    data object MoveFileNotFound : Failure(
        explanationStringRes = R.string.file_has_already_been_moved_or_deleted,
        cancelMoveFileNotification = true
    )

    data object InternalError : Failure(
        explanationStringRes = R.string.internal_error,
        cancelMoveFileNotification = false
    )

    data object FileAlreadyAtDestination : Failure(
        explanationStringRes = R.string.file_already_at_selected_location,
        cancelMoveFileNotification = false
    )

    data object NotEnoughSpaceOnDestination : Failure(
        explanationStringRes = R.string.not_enough_space_on_destination,
        cancelMoveFileNotification = false
    )

    data object MoveDestinationNotFound : Failure(false, null)

    infix fun bundleWith(moveBundle: AnyMoveBundle): Bundle =
        Bundle.PostMoveBundleCreation(this, moveBundle)

    sealed interface Bundle {

        data class PreCheckFailure(
            val moveFailure: Failure,
            val notificationResources: NotificationResources?
        ) : Bundle

        data class PostMoveBundleCreation(
            val moveResult: MoveResult,
            val moveBundle: AnyMoveBundle
        ) : Bundle
    }
}
