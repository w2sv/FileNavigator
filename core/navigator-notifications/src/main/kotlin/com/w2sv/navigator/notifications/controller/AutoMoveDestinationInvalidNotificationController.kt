package com.w2sv.navigator.notifications.controller

import androidx.core.app.NotificationCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.core.common.R
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.navigator.domain.notifications.NotificationEvent
import com.w2sv.navigator.notifications.AppNotification
import com.w2sv.navigator.notifications.NotificationEventReceiver
import com.w2sv.navigator.notifications.api.MultiNotificationController
import com.w2sv.navigator.notifications.api.NotificationEnvironment
import com.w2sv.navigator.notifications.helper.iconBitmap
import javax.inject.Inject

internal class AutoMoveDestinationInvalidNotificationController @Inject constructor(
    environment: NotificationEnvironment,
    private val moveDestinationPathConverter: MoveDestinationPathConverter
) : MultiNotificationController<NotificationEvent.AutoMoveDestinationInvalid>(
    environment = environment,
    appNotification = AppNotification.AutoMoveDestinationInvalid
) {
    override fun NotificationCompat.Builder.configure(args: NotificationEvent.AutoMoveDestinationInvalid, id: Int) {
        setContentTitle(context.getString(R.string.auto_move_destination_invalid))

        // Set file source icon
        setLargeIcon(args.fileAndSourceType.iconBitmap(context))
        setContentText(contentText(args))

        setDeleteIntent(
            NotificationEventReceiver.pendingIntent(
                context = context,
                requestCode = id,
                event = NotificationEvent.CancelAutoMoveDestinationInvalid(id)
            )
        )
    }

    private fun contentText(args: NotificationEvent.AutoMoveDestinationInvalid) =
        buildSpannedString {
            bold {
                append(
                    moveDestinationPathConverter.invoke(
                        moveDestination = args.destination,
                        context = context
                    )
                )
            }
            append(" ${context.getString(R.string.has_been_deleted_and_got_therefore_removed_from)} ")
            bold { append(args.fileAndSourceType.fileType.label(context)) }
            append(" -> ")
            bold { append(context.getString(args.fileAndSourceType.sourceType.labelRes)) }
        }
}
