package com.w2sv.navigator.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.w2sv.data.model.FileType
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.R
import com.w2sv.navigator.actions.FileDeletionBroadcastReceiver
import com.w2sv.navigator.actions.FileMoveActivity
import com.w2sv.navigator.actions.MoveToDefaultDestinationBroadcastReceiver
import com.w2sv.navigator.model.MoveFile

class NewMoveFileNotificationManager(
    private val context: Context,
    notificationManager: NotificationManager
) : AppNotificationManager(
    notificationChannel = getNotificationChannel(
        id = "NEW_MOVE_FILE",
        name = context.getString(R.string.new_file_detected)
    ),
    notificationManager = notificationManager,
    resourcesBaseSeed = 1
) {
    fun buildAndEmit(
        moveFile: MoveFile,
        getDefaultMoveDestination: (FileType.Source) -> Uri?
    ) {
        val resources = getNotificationResources(4)
        notificationManager.notify(
            resources.id,
            Builder(
                moveFile = moveFile,
                getDefaultMoveDestination = getDefaultMoveDestination,
                resources = resources
            )
                .build()
        )
    }

    inner class Builder(
        private val moveFile: MoveFile,
        private val getDefaultMoveDestination: (FileType.Source) -> Uri?,
        private val resources: NotificationResources
    ) : NotificationCompat.Builder(context, notificationChannel.id) {

        override fun build(): Notification {
            priority = NotificationCompat.PRIORITY_DEFAULT

            setContentTitle(
                context.getString(
                    R.string.new_file_detected_template,
                    getNotificationTitleFormatArg()
                )
            )
            // set icons
            setSmallIcon(moveFile.type.iconRes)
            setLargeIcon(
                AppCompatResources.getDrawable(
                    context,
                    moveFile.sourceKind.iconRes
                )
                    ?.toBitmap()
            )
            // set content
            val notificationContentText =
                context.getString(
                    R.string.found_at,
                    moveFile.data.name,
                    moveFile.data.relativePath
                )

            setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notificationContentText)
            )
            setContentText(notificationContentText)

            addActions()

            return super.build()
        }

        private fun getNotificationTitleFormatArg() =
            when (val fileType = moveFile.type) {
                is FileType.Media -> {
                    when (moveFile.data.getSourceKind()) {
                        FileType.Source.Kind.Screenshot -> context.getString(
                            R.string.new_screenshot
                        )

                        FileType.Source.Kind.Camera -> context.getString(
                            when (moveFile.type) {
                                FileType.Media.Image -> R.string.new_photo
                                FileType.Media.Video -> R.string.new_video
                                else -> throw Error()
                            }
                        )

                        FileType.Source.Kind.Download -> context.getString(
                            R.string.newly_downloaded_template,
                            context.getString(fileType.fileDeclarationRes)
                        )

                        FileType.Source.Kind.OtherApp -> context.getString(
                            R.string.new_third_party_file_template,
                            moveFile.data.dirName,
                            context.getString(fileType.fileDeclarationRes)
                        )
                    }
                }

                is FileType.NonMedia -> {
                    context.getString(R.string.new_file, context.getString(moveFile.type.titleRes))
                }
            }

        private fun addActions() {
            val requestCodeIterator = resources.actionRequestCodes.iterator()

            addAction(getViewFileAction(requestCodeIterator.next()))
            addAction(getMoveFileAction(requestCodeIterator.next(), resources))
            getDefaultMoveDestination(moveFile.source)?.let {
                addAction(
                    getMoveToDefaultDestinationAction(
                        requestCodeIterator.next(),
                        resources,
                        it
                    )
                )
            }
            addAction(getDeleteFileAction(requestCodeIterator.next(), resources))
        }

        private fun getViewFileAction(requestCode: Int)
                : NotificationCompat.Action =
            NotificationCompat.Action(
                R.drawable.ic_file_open_24,
                context.getString(R.string.view),
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    Intent()
                        .setAction(Intent.ACTION_VIEW)
                        .setDataAndType(
                            moveFile.uri,
                            moveFile.type.mediaType.mimeType
                        ),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )

        private fun getMoveFileAction(
            requestCode: Int,
            notificationResources: NotificationResources
        ): NotificationCompat.Action =
            NotificationCompat.Action(
                R.drawable.ic_file_move_24,
                context.getString(R.string.move),
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    Intent.makeRestartActivityTask(
                        ComponentName(
                            context,
                            FileMoveActivity::class.java
                        )
                    )
                        .putExtra(FileNavigator.EXTRA_MOVE_FILE, moveFile)
                        .putExtra(
                            NotificationResources.EXTRA,
                            notificationResources
                        ),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )

        private fun getMoveToDefaultDestinationAction(
            requestCode: Int,
            notificationResources: NotificationResources,
            defaultMoveDestination: Uri
        ): NotificationCompat.Action =
            NotificationCompat.Action(
                R.drawable.ic_add_new_folder_24,
                context.getString(R.string.move_to_default_destination),
                PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    Intent(
                        context,
                        MoveToDefaultDestinationBroadcastReceiver::class.java
                    )
                        .putExtra(FileNavigator.EXTRA_MOVE_FILE, moveFile)
                        .putExtra(
                            NotificationResources.EXTRA,
                            notificationResources
                        )
                        .putExtra(
                            FileNavigator.EXTRA_DEFAULT_MOVE_DESTINATION,
                            defaultMoveDestination
                        ),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                )
            )

        private fun getDeleteFileAction(
            requestCode: Int,
            notificationResources: NotificationResources
        ): NotificationCompat.Action =
            NotificationCompat.Action(
                R.drawable.ic_delete_24,
                context.getString(R.string.delete),
                PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    Intent(
                        context,
                        FileDeletionBroadcastReceiver::class.java
                    )
                        .putExtra(FileNavigator.EXTRA_MOVE_FILE, moveFile)
                        .putExtra(
                            NotificationResources.EXTRA,
                            notificationResources
                        ),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                )
            )
    }
}