package com.w2sv.navigator.notifications.managers

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.core.navigator.R
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.AppNotificationId
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.MultiInstanceNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AutoMoveDestinationInvalidNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager,
    private val moveDestinationPathConverter: MoveDestinationPathConverter
) : MultiInstanceNotificationManager<AutoMoveDestinationInvalidNotificationManager.Args>(
    appNotificationChannel = AppNotificationChannel.AutoMoveDestinationInvalid,
    notificationManager = notificationManager,
    context = context,
    appNotificationId = AppNotificationId.AutoMoveDestinationInvalid
) {
    data class Args(
        val fileAndSourceType: FileAndSourceType,
        val autoMoveDestination: MoveDestination.Directory,
        override val resources: NotificationResources
    ) : MultiInstanceNotificationManager.Args

    fun buildAndPostNotification(
        fileAndSourceType: FileAndSourceType,
        autoMoveDestination: MoveDestination.Directory,
    ) {
        buildNotification(
            Args(
                fileAndSourceType = fileAndSourceType,
                autoMoveDestination = autoMoveDestination,
                resources = getNotificationResources()
            )
        )
    }

    override fun getBuilder(args: Args): AppNotificationManager<Args>.Builder {
        return object : Builder() {
            override fun build(): Notification {
                setContentTitle(context.getString(R.string.auto_move_destination_invalid))

                // Set file source icon
                setLargeIcon(args.fileAndSourceType.coloredIconBitmap(context))
                setContentText(
                    buildSpannedString {
                        bold {
                            append(
                                moveDestinationPathConverter.invoke(
                                    moveDestination = args.autoMoveDestination,
                                    context = context
                                )
                            )
                        }
                        append(" ${context.getString(R.string.has_been_deleted_and_got_therefore_removed_from)} ")
                        bold {
                            append(context.getString(args.fileAndSourceType.fileType.labelRes))
                        }
                        append(" -> ")
                        bold {
                            append(context.getString(args.fileAndSourceType.sourceType.labelRes))
                        }
                    }
                )

                setDeleteIntent(
                    getCleanupNotificationResourcesPendingIntent(
                        requestCode = args.resources.pendingIntentRequestCodes(1).first(),
                        notificationResources = args.resources
                    )
                )

                return super.build()
            }
        }
    }
}