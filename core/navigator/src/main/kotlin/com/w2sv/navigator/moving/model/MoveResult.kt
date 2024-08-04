package com.w2sv.navigator.moving.model

import androidx.annotation.StringRes
import com.w2sv.core.navigator.R

internal sealed class MoveResult(val cancelNotification: Boolean) {

    data object Success : MoveResult(true)

    sealed class Failure(
        cancelNotification: Boolean,
        @StringRes val explanationStringRes: Int?
    ) : MoveResult(cancelNotification) {

        data object ManageAllFilesPermissionMissing :
            Failure(
                explanationStringRes = R.string.manage_all_files_permission_missing,
                cancelNotification = false
            )

        data object MoveFileNotFound : Failure(
            explanationStringRes = R.string.file_has_already_been_moved_or_deleted,
            cancelNotification = true
        )

        data object InternalError : Failure(
            explanationStringRes = R.string.internal_error,
            cancelNotification = false
        )

        data object FileAlreadyAtDestination : Failure(
            explanationStringRes = R.string.file_already_at_selected_location,
            cancelNotification = true
        )

        data object NotEnoughSpaceOnDestination : Failure(
            explanationStringRes = R.string.not_enough_space_on_destination,
            cancelNotification = false
        )

        data object MoveDestinationNotFound : Failure(false, null)
    }
}