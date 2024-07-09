package com.w2sv.navigator.moving.model

import com.w2sv.common.utils.ToastProperties
import com.w2sv.core.navigator.R

internal sealed class MoveResult(val cancelNotification: Boolean?) {

    sealed class Failure(cancelNotification: Boolean?) :
        MoveResult(cancelNotification = cancelNotification) {

        sealed class Generic(
            val toastProperties: ToastProperties,
            cancelNotification: Boolean
        ) : Failure(cancelNotification)

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

        data object FileAlreadyAtDestination : Generic(
            toastProperties = ToastProperties(R.string.file_already_at_selected_location),
            cancelNotification = true
        )

        data object NotEnoughSpaceOnDestination : Generic(
            toastProperties = ToastProperties(R.string.not_enough_space_on_destination),
            cancelNotification = false
        )

        data class MoveDestinationNotFound(val moveBundle: MoveBundle) : Failure(null)
    }

    data class Success(val moveBundle: MoveBundle) : MoveResult(true)
}