package com.w2sv.navigator.notifications.appnotificationmanager

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.data.model.FileType
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.R
import com.w2sv.navigator.actionexecutors.FileMoveActivity
import com.w2sv.navigator.actionexecutors.receivers.FileDeletionBroadcastReceiver
import com.w2sv.navigator.actionexecutors.receivers.MoveToDefaultDestinationBroadcastReceiver
import com.w2sv.navigator.actionexecutors.receivers.NotificationResourcesCleanupBroadcastReceiver
import com.w2sv.navigator.model.MoveFile
import com.w2sv.navigator.notifications.AppNotificationsManager
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.getNotificationChannel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class NewMoveFileNotificationManager(
    context: Context,
    notificationManager: NotificationManager
) : MultiInstanceAppNotificationManager<NewMoveFileNotificationManager.Args>(
    notificationChannel = getNotificationChannel(
        id = "NEW_MOVE_FILE",
        name = context.getString(R.string.new_file_detected)
    ),
    notificationManager = notificationManager,
    context = context,
    resourcesBaseSeed = 1
) {
    inner class Args(
        val moveFile: MoveFile,
        val getDefaultMoveDestination: (FileType.Source) -> Uri?,
        val resources: NotificationResources = getNotificationResources(5)
    ) : AppNotificationManager.Args

    override fun getBuilder(args: Args): Builder =
        object : Builder() {

            private val moveFile by args::moveFile
            private val getDefaultMoveDestination by args::getDefaultMoveDestination
            private val resources by args::resources

            override fun build(): Notification {
                setContentTitle(
                    getContentTitle()
                )
                // Set icons
                setSmallIcon(R.drawable.ic_app_logo_24)
                setLargeIcon(
                    AppCompatResources.getDrawable(context, getLargeIconDrawable())
                        ?.apply { setTint(moveFile.type.colorInt) }
                        ?.toBitmap()
                )
                // Set content
                setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            buildSpannedString {
                                bold { append(moveFile.data.name) }
                                append(" ${context.getString(R.string.found_at)} ")
                                bold { append(moveFile.data.relativePath) }
                            }
                        )
                )

                // Set actions & intents
                val requestCodeIterator = resources.actionRequestCodes.iterator()

                addActions(requestCodeIterator)

                setDeleteIntent(
                    getCleanupNotificationResourcesPendingIntent(
                        requestCode = requestCodeIterator.next(),
                    )
                )

                return super.build()
            }

            private fun getContentTitle() =
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
                                context.getString(fileType.titleRes)
                            )

                            FileType.Source.Kind.OtherApp -> context.getString(
                                R.string.new_third_party_file_template,
                                moveFile.data.dirName,
                                context.getString(fileType.titleRes)
                            )
                        }
                    }

                    is FileType.NonMedia -> {
                        context.getString(
                            R.string.new_,
                            context.getString(moveFile.type.titleRes)
                        )
                    }
                }

            @DrawableRes
            private fun getLargeIconDrawable(): Int =
                when (moveFile.sourceKind) {
                    FileType.Source.Kind.Screenshot -> moveFile.sourceKind.iconRes
                    else -> moveFile.type.iconRes
                }

            private fun addActions(requestCodeIterator: Iterator<Int>) {
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
                    R.drawable.ic_app_logo_24,
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
                    R.drawable.ic_app_logo_24,
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

            private fun getCleanupNotificationResourcesPendingIntent(
                requestCode: Int
            ): PendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    ResourcesCleanupBroadcastReceiver.getIntent(
                        context,
                        resources
                    ),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                )
        }

    fun buildAndEmit(args: Args) {
        super.buildAndEmit(args.resources.id, args)
    }

    @AndroidEntryPoint
    class ResourcesCleanupBroadcastReceiver : NotificationResourcesCleanupBroadcastReceiver() {

        @Inject
        lateinit var appNotificationsManager: AppNotificationsManager

        override val multiInstanceAppNotificationManager: MultiInstanceAppNotificationManager<*>
            get() = appNotificationsManager.newMoveFileNotificationManager

        companion object {
            fun getIntent(
                context: Context,
                notificationResources: NotificationResources
            ): Intent =
                getIntent<ResourcesCleanupBroadcastReceiver>(
                    context,
                    notificationResources
                )

            fun startFromResourcesComprisingIntent(
                context: Context,
                intent: Intent
            ) {
                startFromResourcesComprisingIntent<ResourcesCleanupBroadcastReceiver>(
                    context,
                    intent
                )
            }
        }
    }
}

fun Context.combineDrawables(drawable1Id: Int, drawable2Id: Int): Bitmap {
    // Load the drawables from resources
    val drawable1: Drawable = AppCompatResources.getDrawable(this, drawable1Id)!!
    val drawable2: Drawable = AppCompatResources.getDrawable(this, drawable2Id)!!

    val width = drawable1.intrinsicWidth
    val height = drawable1.intrinsicHeight

    // Create a blank bitmap with the specified width and height
    val combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    // Create a canvas for drawing on the bitmap
    val canvas = Canvas(combinedBitmap)

    // Calculate the width for each drawable
    val drawableWidth = width / 2

    // Draw the first drawable at the left side
    drawable1.setBounds(0, 0, drawableWidth, height)
    drawable1.draw(canvas)

    // Draw the second drawable to the right of the first drawable
    drawable2.setBounds(drawableWidth, 0, width, height)
    drawable2.draw(canvas)

    return combinedBitmap
}