package com.w2sv.navigator.moving.model

import androidx.annotation.StringRes
import com.w2sv.core.navigator.R

internal sealed class MoveResult(val cancelNewMoveFileNotification: Boolean) {

    sealed class Failure(cancelNotification: Boolean) :
        MoveResult(cancelNewMoveFileNotification = cancelNotification) {

        sealed class Generic(
            @StringRes val explanationStringRes: Int,
            cancelNewMoveFileNotification: Boolean
        ) : Failure(cancelNewMoveFileNotification)

        data object ManageAllFilesPermissionMissing :
            Generic(
                explanationStringRes = R.string.manage_all_files_permission_missing,
                cancelNewMoveFileNotification = false
            )

        data object MoveFileNotFound : Generic(
            explanationStringRes = R.string.file_has_already_been_moved_or_deleted,
            cancelNewMoveFileNotification = true
        )

        data object InternalError : Generic(
            explanationStringRes = R.string.internal_error,
            cancelNewMoveFileNotification = false
        )

        data object FileAlreadyAtDestination : Generic(
            explanationStringRes = R.string.file_already_at_selected_location,
            cancelNewMoveFileNotification = true
        )

        data object NotEnoughSpaceOnDestination : Generic(
            explanationStringRes = R.string.not_enough_space_on_destination,
            cancelNewMoveFileNotification = false
        )

        data class MoveDestinationNotFound(val moveBundle: MoveBundle) : Failure(false)
    }

    data class Success(val moveBundle: MoveBundle) : MoveResult(true)
}