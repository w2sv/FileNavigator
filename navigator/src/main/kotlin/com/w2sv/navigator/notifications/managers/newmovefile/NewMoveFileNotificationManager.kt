package com.w2sv.navigator.notifications.managers.newmovefile

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.common.utils.getDocumentUriFileName
import com.w2sv.common.utils.whiteSpaceWrapped
import com.w2sv.data.model.FileType
import com.w2sv.navigator.R
import com.w2sv.navigator.model.MoveFile
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.getNotificationChannel
import com.w2sv.navigator.notifications.managers.AppNotificationsManager
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.MultiInstanceAppNotificationManager
import com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.FileMoveActivity
import com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.receivers.NotificationResourcesCleanupBroadcastReceiver
import com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.receivers.QuickMoveBroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i
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
        val getLastMoveDestination: (FileType.Source) -> Uri?,
        val resources: NotificationResources = getNotificationResources(4)
    ) : AppNotificationManager.Args

    override fun getBuilder(args: Args): Builder =
        object : Builder() {

            private val navigatableFile by args::moveFile
            private val getLastMoveDestination by args::getLastMoveDestination
            private val resources by args::resources

            override fun build(): Notification {
                setGroup("GROUP")

                setContentTitle(
                    getContentTitle()
                )
                // Set icons
                setSmallIcon(R.drawable.ic_app_logo_24)
                setLargeIcon(
                    AppCompatResources.getDrawable(context, getLargeIconDrawable())
                        ?.apply { setTint(navigatableFile.type.colorInt) }
                        ?.toBitmap()
                )
                // Set content
                setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            buildSpannedString {
                                bold { append(navigatableFile.mediaStoreFile.columnData.name) }
                                append(context.getString(R.string.found_at).whiteSpaceWrapped())
                                bold { append(navigatableFile.mediaStoreFile.columnData.volumeRelativeDirPath) }
                            }
                        )
                )

                // Set actions & intents
                val requestCodeIterator = resources.actionRequestCodes.iterator()

                addActions(requestCodeIterator)

                setContentIntent(getViewFilePendingIntent(requestCodeIterator.next()))

                setDeleteIntent(
                    getCleanupNotificationResourcesPendingIntent(
                        requestCode = requestCodeIterator.next(),
                    )
                )

                return super.build()
            }

            private fun getContentTitle() =
                when (val fileType = navigatableFile.type) {
                    is FileType.Media -> {
                        when (navigatableFile.mediaStoreFile.columnData.getSourceKind()) {
                            FileType.Source.Kind.Screenshot -> context.getString(
                                R.string.new_screenshot
                            )

                            FileType.Source.Kind.Camera -> context.getString(
                                when (navigatableFile.type) {
                                    FileType.Media.Image -> R.string.new_photo
                                    FileType.Media.Video -> R.string.new_video
                                    else -> throw Error()
                                }
                            )

                            FileType.Source.Kind.Download -> context.getString(
                                R.string.new_downloaded_template,
                                context.getString(fileType.titleRes)
                            )

                            FileType.Source.Kind.OtherApp -> context.getString(
                                R.string.new_third_party_file_template,
                                navigatableFile.mediaStoreFile.columnData.dirName,
                                context.getString(fileType.titleRes)
                            )
                        }
                    }

                    is FileType.NonMedia -> {
                        context.getString(
                            R.string.new_,
                            context.getString(navigatableFile.type.titleRes)
                        )
                    }
                }

            @DrawableRes
            private fun getLargeIconDrawable(): Int =
                when (navigatableFile.sourceKind) {
                    FileType.Source.Kind.Screenshot, FileType.Source.Kind.Camera -> navigatableFile.sourceKind.iconRes
                    else -> navigatableFile.type.iconRes
                }

            private fun addActions(requestCodeIterator: Iterator<Int>) {
                addAction(getMoveFileAction(requestCodeIterator.next()))
                getLastMoveDestination(navigatableFile.source)?.let {
                    addAction(
                        getQuickMoveAction(
                            requestCodeIterator.next(),
                            it
                        )
                    )
                }
            }

            private fun getViewFilePendingIntent(requestCode: Int)
                    : PendingIntent =
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    Intent()
                        .setAction(Intent.ACTION_VIEW)
                        .setDataAndType(
                            navigatableFile.mediaStoreFile.uri,
                            navigatableFile.type.simpleStorageMediaType.mimeType
                        ),
                    PendingIntent.FLAG_IMMUTABLE
                )

            private fun getMoveFileAction(
                requestCode: Int,
            ): NotificationCompat.Action =
                NotificationCompat.Action(
                    R.drawable.ic_app_logo_24,
                    context.getString(R.string.move),
                    PendingIntent.getActivity(
                        context,
                        requestCode,
                        FileMoveActivity.makeRestartActivityIntent(
                            navigatableFile,
                            resources,
                            context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

            private fun getQuickMoveAction(
                requestCode: Int,
                moveDestination: Uri
            ): NotificationCompat.Action =
                NotificationCompat.Action(
                    R.drawable.ic_app_logo_24,
                    getDocumentUriFileName(moveDestination, context)?.let { "/$it" },
                    PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        QuickMoveBroadcastReceiver.getIntent(
                            navigatableFile,
                            resources,
                            moveDestination.also {
                                i { "MoveDestination Extra: $it" }
                            },
                            context
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
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