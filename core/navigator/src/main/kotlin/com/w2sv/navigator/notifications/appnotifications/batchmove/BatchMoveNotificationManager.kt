package com.w2sv.navigator.notifications.appnotifications.batchmove

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.res.isNightModeActiveCompat
import com.w2sv.core.common.R
import com.w2sv.navigator.moving.api.activity.AbstractDestinationPickerActivity
import com.w2sv.navigator.moving.batch.BatchMoveBroadcastReceiver
import com.w2sv.navigator.moving.model.DestinationSelectionManner
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveDestination
import com.w2sv.navigator.notifications.api.SingleInstanceNotificationManager
import com.w2sv.navigator.notifications.appnotifications.AppNotificationChannel
import com.w2sv.navigator.notifications.appnotifications.AppNotificationId
import com.w2sv.navigator.notifications.appnotifications.drawableBitmap
import com.w2sv.navigator.notifications.appnotifications.movefile.MoveFileNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class BatchMoveNotificationManager @Inject constructor(
    @ApplicationContext context: Context,
    notificationManager: NotificationManager
) : SingleInstanceNotificationManager<Collection<MoveFileNotificationManager.Args>>(
    appNotificationChannel = AppNotificationChannel.NewNavigatableFile,
    notificationManager = notificationManager,
    context = context,
    appNotificationId = AppNotificationId.BatchMoveFiles
) {
    private enum class PendingIntentRequestCode {
        MoveAction,
        QuickMoveAction
    }

    override fun getBuilder(args: Collection<MoveFileNotificationManager.Args>): Builder =
        object : Builder() {
            override fun build(): Notification {
                setGroup(notificationChannel.id)
                setContentTitle(
                    context.getString(
                        R.string.move_files,
                        args.size
                    )
                )
                setLargeIcon(
                    context.drawableBitmap(
                        drawable = R.drawable.ic_files_24,
                        tint = if (context.resources.configuration.isNightModeActiveCompat) null else Color.BLACK
                    )
                )
                setSilent(true)
                addAction(getMoveFilesAction())
                addQuickMoveActions()
                return super.build()
            }

            private fun getMoveFilesAction(): NotificationCompat.Action =
                NotificationCompat.Action(
                    com.w2sv.core.common.R.drawable.ic_app_logo_24,
                    context.getString(R.string.move),
                    PendingIntent.getActivity(
                        context,
                        PendingIntentRequestCode.MoveAction.ordinal,
                        AbstractDestinationPickerActivity.makeRestartActivityIntent<BatchMoveDestinationPickerActivity>(
                            BatchMoveDestinationPickerActivity.Args(
                                moveFilesWithNotificationResources = args.map { // TODO: optimizable?
                                    BatchMoveDestinationPickerActivity.MoveFileWithNotificationResources(
                                        it.moveFile,
                                        it.notificationResources
                                    )
                                },
                                pickerStartDestination = null
                            ),
                            context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

            private fun addQuickMoveActions() {
                val occurrenceOrderedQuickMoveDestinations =
                    args
                        .flatMap { it.quickMoveDestinations }
                        .groupingBy { it }
                        .eachCount().entries.sortedByDescending { it.value }
                        .map { it.key }
                var addedQuickMoveActions = 0
                for (moveDestination in occurrenceOrderedQuickMoveDestinations) {
                    // Don't add action if folder doesn't exist anymore, which results in getDocumentUriFileName returning null.
                    moveDestination.documentFile(context).name?.let { directoryName ->
                        addAction(
                            getQuickMoveAction(
                                destination = moveDestination,
                                destinationDirectoryName = directoryName,
                                requestCode = PendingIntentRequestCode.QuickMoveAction.ordinal + addedQuickMoveActions
                            )
                        )
                        addedQuickMoveActions += 1
                        if (addedQuickMoveActions == 2) {
                            return
                        }
                    }
                }
            }

            private fun getQuickMoveAction(
                destination: MoveDestination.Directory,
                destinationDirectoryName: String,
                requestCode: Int
            ): NotificationCompat.Action =
                NotificationCompat.Action(
                    com.w2sv.core.common.R.drawable.ic_app_logo_24,
                    context.getString(R.string.to, destinationDirectoryName),
                    PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        BatchMoveBroadcastReceiver.getIntent(
                            args = BatchMoveBroadcastReceiver.Args(
                                batchMoveBundles = args.map { // TODO: optimizable?
                                    MoveBundle.QuickMove(
                                        file = it.moveFile,
                                        destinationSelectionManner = DestinationSelectionManner.Quick(
                                            notificationResources = it.notificationResources
                                        ),
                                        destination = destination,
                                        batched = true
                                    )
                                }
                            ),
                            context = context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
        }

    fun cancelOrUpdate(newMoveFileNotificationBuilderArgs: Collection<MoveFileNotificationManager.Args>) {
        if (newMoveFileNotificationBuilderArgs.size <= 1) {
            cancelNotification()
        } else {
            buildAndPostNotification(newMoveFileNotificationBuilderArgs)
        }
    }
}
