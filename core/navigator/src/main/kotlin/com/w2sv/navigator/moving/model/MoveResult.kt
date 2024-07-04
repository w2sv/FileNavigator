package com.w2sv.navigator.moving.model

import com.w2sv.common.utils.ToastProperties
import com.w2sv.core.navigator.R

internal interface MoveResult {
    sealed interface Failure :
        MoveResult {

        sealed class Generic(
            val toastProperties: ToastProperties,
            val cancelNotification: Boolean
        ) : Failure

        data object ManageAllFilesPermissionMissing :
            Generic(
                toastProperties = ToastProperties(message = R.string.couldnt_move_manage_all_files_permission_missing),
                cancelNotification = false
            )

        data object MoveFileNotFound : Generic(
            toastProperties = ToastProperties(
                message = R.string.file_has_already_been_moved_or_deleted,
            ),
            cancelNotification = true
        )

        data object InternalError : Generic(
            toastProperties = ToastProperties(R.string.couldnt_move_file_internal_error),
            cancelNotification = false
        )

        data object FileAlreadyAtMoveDestination : Generic(
            toastProperties = ToastProperties(R.string.file_already_at_selected_location),
            cancelNotification = true
        )

        data class AutoMoveDestinationNotFound(val moveBundle: MoveBundle) : Failure
    }

    data class Success(val moveBundle: MoveBundle) : MoveResult
}