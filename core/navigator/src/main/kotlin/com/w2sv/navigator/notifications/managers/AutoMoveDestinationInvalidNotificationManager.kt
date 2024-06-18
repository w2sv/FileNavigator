package com.w2sv.navigator.notifications.managers

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.common.utils.DocumentUri
import com.w2sv.core.navigator.R
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.usecase.DocumentUriToPathConverter
import com.w2sv.navigator.notifications.AppNotificationChannel
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.MultiInstanceAppNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AutoMoveDestinationInvalidNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager,
    private val documentUriToPathConverter: DocumentUriToPathConverter
) : MultiInstanceAppNotificationManager<AutoMoveDestinationInvalidNotificationManager.BuilderArgs>(
    notificationChannel = AppNotificationChannel.NewNavigatableFile.getNotificationChannel(context),
    notificationManager = notificationManager,
    context = context,
    resourcesBaseSeed = 2
) {
    inner class BuilderArgs(
        val fileAndSourceType: FileAndSourceType,
        val autoMoveDestination: DocumentUri
    ) : MultiInstanceAppNotificationManager.BuilderArgs(
        resources = getNotificationResources(
            pendingIntentRequestCodeCount = 1
        )
    )

    override fun getBuilder(args: AutoMoveDestinationInvalidNotificationManager.BuilderArgs): AppNotificationManager<BuilderArgs>.Builder {
        return object : Builder() {
            override fun build(): Notification {
                setContentTitle(context.getString(R.string.auto_move_destination_invalid))

                // Set file source icon
                setLargeIcon(args.fileAndSourceType.coloredIconBitmap(context))
                setContentText(
                    buildSpannedString {
                        bold {
                            append(
                                documentUriToPathConverter.invoke(
                                    documentUri = args.autoMoveDestination,
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
                        requestCode = args.resources.pendingIntentRequestCodes.first(),
                        notificationResources = args.resources
                    )
                )

                return super.build()
            }
        }
    }
}