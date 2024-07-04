package com.w2sv.navigator.moving.model

import com.w2sv.common.utils.ToastProperties
import com.w2sv.core.navigator.R

internal sealed interface MoveResult {
    sealed class Failure(val toastProperties: ToastProperties, val cancelNotification: Boolean) :
        MoveResult {

        data object ManageAllFilesPermissionMissing : Failure(
            toastProperties = ToastProperties(
                message = R.string.couldnt_move_manage_all_files_permission_missing,
            ),
            cancelNotification = false
        )

        data object MoveFileNotFound : Failure(
            toastProperties = ToastProperties(
                message = R.string.file_has_already_been_moved_or_deleted,
            ),
            cancelNotification = true
        )

        data object InternalError : Failure(
            toastProperties = ToastProperties(R.string.couldnt_move_file_internal_error),
            cancelNotification = false
        )

        data object FileAlreadyAtMoveDestination : Failure(
            toastProperties = ToastProperties(R.string.file_already_at_selected_location),
            cancelNotification = true
        )

        data class AutoMoveDestinationNotFound : Failure()
    }

    data class Success(val moveBundle: MoveBundle) : MoveResult
}