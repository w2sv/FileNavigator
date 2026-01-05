package com.w2sv.navigator.domain.moving

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.androidutils.res.getHtmlFormattedText
import com.w2sv.core.common.R
import com.w2sv.navigator.domain.notifications.NotificationEvent

sealed interface MoveOperationFeedback {
    data class Toast(val text: CharSequence, val duration: Int) : MoveOperationFeedback

    @JvmInline
    value class Notification(val event: NotificationEvent) : MoveOperationFeedback
}

sealed class MoveResult(val cancelNotification: Boolean = false, val makeFeedback: Context.(MoveOperation?) -> MoveOperationFeedback) {

    data object Success : MoveResult(
        cancelNotification = true,
        makeFeedback = { successToast(checkNotNull(it)) }
    )

    data object InternalError : MoveResult(makeFeedback = { failureToast(R.string.internal_error) })

    data object ManageAllFilesPermissionMissing : MoveResult(makeFeedback = { failureToast(R.string.manage_all_files_permission_missing) })

    data object MoveFileNotFound :
        MoveResult(cancelNotification = true, makeFeedback = { failureToast(R.string.file_has_already_been_moved_or_deleted) })

    data object FileAlreadyAtDestination : MoveResult(makeFeedback = { failureToast(R.string.file_already_at_selected_location) })

    data object NotEnoughSpaceOnDestination : MoveResult(makeFeedback = { failureToast(R.string.not_enough_space_on_destination) })

    data object MoveDestinationNotFound : MoveResult(
        makeFeedback = { operation ->
            checkNotNull(operation) // TODO
            MoveOperationFeedback.Notification(
                event = NotificationEvent.AutoMoveDestinationInvalid(
                    destination = operation.destination,
                    fileAndSourceType = operation.file.fileAndSourceType
                )
            )
        }
    )
}

private fun Context.failureToast(@StringRes text: Int): MoveOperationFeedback.Toast =
    MoveOperationFeedback.Toast(
        text = buildSpannedString {
            bold { append("${getString(R.string.couldnt_move)}: ") }
            append(getString(text))
        },
        duration = Toast.LENGTH_LONG
    )

private fun Context.successToast(operation: MoveOperation): MoveOperationFeedback.Toast =
    MoveOperationFeedback.Toast(
        text = resources.getHtmlFormattedText(
            id = if (operation.destinationSelectionManner.isAuto) {
                R.string.auto_move_success_toast_text
            } else {
                R.string.move_success_toast_text
            },
            operation.file.fileAndSourceType.label(
                context = this,
                isGif = operation.file.isGif
            ),
            operation.destination.uiRepresentation(this)
        ),
        duration = Toast.LENGTH_SHORT
    )
