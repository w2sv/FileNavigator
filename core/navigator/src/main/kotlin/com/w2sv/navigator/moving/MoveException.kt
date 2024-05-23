package com.w2sv.navigator.moving

import com.w2sv.common.utils.ToastProperties
import com.w2sv.core.navigator.R

enum class MoveException(val toastProperties: ToastProperties, val cancelNotification: Boolean) {

    MissingManageAllFilesPermission(
        toastProperties = ToastProperties(
            message = R.string.couldnt_move_manage_all_files_permission_missing,
        ),
        cancelNotification = false
    ),

    /**
     * File has already been moved/deleted in between notification emission & FileMoveActivity invocation
     */
    MoveFileNotFound(
        toastProperties = ToastProperties(
            message = R.string.file_has_already_been_moved_or_deleted,
        ),
        cancelNotification = true
    ),
    InternalError(
        toastProperties = ToastProperties(R.string.couldnt_move_file_internal_error),
        cancelNotification = false
    ),
    FileAlreadyAtMoveDestination(
        toastProperties = ToastProperties(R.string.file_already_at_selected_location),
        cancelNotification = true
    )
}