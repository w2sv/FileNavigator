package com.w2sv.navigator.moving.model

import androidx.annotation.StringRes
import com.w2sv.core.navigator.R

internal sealed class MoveResult(val cancelNotification: Boolean) {

    sealed class Failure(cancelNotification: Boolean) :
        MoveResult(cancelNotification = cancelNotification) {

        sealed class Generic(
            @StringRes val explanationStringRes: Int,
            cancelNotification: Boolean
        ) : Failure(cancelNotification)

        data object ManageAllFilesPermissionMissing :
            Generic(
                explanationStringRes = R.string.manage_all_files_permission_missing,
                cancelNotification = false
            )

        data object MoveFileNotFound : Generic(
            explanationStringRes = R.string.file_has_already_been_moved_or_deleted,
            cancelNotification = true
        )

        data object InternalError : Generic(
            explanationStringRes = R.string.internal_error,
            cancelNotification = false
        )

        data object FileAlreadyAtDestination : Generic(
            explanationStringRes = R.string.file_already_at_selected_location,
            cancelNotification = true
        )

        data object NotEnoughSpaceOnDestination : Generic(
            explanationStringRes = R.string.not_enough_space_on_destination,
            cancelNotification = false
        )

        data class MoveDestinationNotFound(val moveBundle: MoveBundle) : Failure(false)
    }

    data class Success(val moveBundle: MoveBundle) : MoveResult(true)
}