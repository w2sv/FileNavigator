package com.w2sv.navigator.fileobservers

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.common.collect.EvictingQueue
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.androidutils.notifying.showNotification
import com.w2sv.common.notifications.createNotificationChannelAndGetNotificationBuilder
import com.w2sv.data.model.FileType
import com.w2sv.data.model.filterEnabled
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.MoveFile
import com.w2sv.navigator.R
import com.w2sv.navigator.actions.FileDeletionBroadcastReceiver
import com.w2sv.navigator.actions.FileMoveActivity
import com.w2sv.navigator.actions.MoveToDefaultDestinationBroadcastReceiver
import slimber.log.i

internal abstract class FileObserver(
    val contentObserverUri: Uri,
    protected val context: Context,
    private val getNotificationParameters: (Int) -> FileNavigator.NotificationParameters,
    private val getDefaultMoveDestination: (FileType.Source) -> Uri?
) :
    ContentObserver(Handler(Looper.getMainLooper())) {

    override fun deliverSelfNotifications(): Boolean = false

    private val mediaStoreFileDataBlacklistCache =
        EvictingQueue.create<MoveFile.MediaStoreData>(5)

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        i { "onChange | Uri: $uri" }

        uri ?: return

        MoveFile.MediaStoreData.fetch(uri, context.contentResolver)?.let { mediaStoreFileData ->
            if (mediaStoreFileData.isPending) return@let

            if (mediaStoreFileData.isNewlyAdded &&
                mediaStoreFileDataBlacklistCache.none {
                    it.pointsToSameContentAs(
                        mediaStoreFileData
                    )
                }
            ) {
                showNotificationIfApplicable(uri, mediaStoreFileData)
            }

            mediaStoreFileDataBlacklistCache.add(mediaStoreFileData)
        }
    }

    protected abstract fun showNotificationIfApplicable(
        uri: Uri,
        mediaStoreFileData: MoveFile.MediaStoreData
    )

    protected fun showDetectedNewFileNotification(moveFile: MoveFile) {
        val notificationContentText =
            context.getString(
                R.string.found_at,
                moveFile.data.name,
                moveFile.data.relativePath
            )

        val notificationParameters = getNotificationParameters(5)

        context.showNotification(
            notificationParameters.notificationId,
            context.createNotificationChannelAndGetNotificationBuilder(
                moveFile.type.notificationChannel
            )
                .setContentTitle(
                    context.getString(
                        R.string.new_file_detected_template,
                        getNotificationTitleFormatArg(moveFile)
                    )
                )
                // set icons
                .setSmallIcon(moveFile.type.iconRes)
                .setLargeIcon(
                    AppCompatResources.getDrawable(
                        context,
                        moveFile.sourceKind.iconRes
                    )
                        ?.toBitmap()
                )
                // set content
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(notificationContentText)
                )
                .setContentText(notificationContentText)
                // add open-file action
                .addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_file_open_24,
                        context.getString(R.string.view),
                        PendingIntent.getActivity(
                            context,
                            notificationParameters.associatedRequestCodes[0],
                            Intent()
                                .setAction(Intent.ACTION_VIEW)
                                .setDataAndType(
                                    moveFile.uri,
                                    moveFile.type.mediaType.mimeType
                                ),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                )
                // add move-file action
                .addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_file_move_24,
                        context.getString(R.string.move),
                        PendingIntent.getActivity(
                            context,
                            notificationParameters.associatedRequestCodes[1],
                            Intent.makeRestartActivityTask(
                                ComponentName(
                                    context,
                                    FileMoveActivity::class.java
                                )
                            )
                                .putExtra(FileNavigator.EXTRA_MOVE_FILE, moveFile)
                                .putExtra(
                                    FileNavigator.NotificationParameters.EXTRA,
                                    notificationParameters
                                ),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                )
                // add move-to-default-destination action
                .apply {
                    val defaultMoveDestination = getDefaultMoveDestination(moveFile.source)

                    if (defaultMoveDestination != null) {
                        addAction(
                            NotificationCompat.Action(
                                R.drawable.ic_add_new_folder_24,
                                context.getString(R.string.move_to_default_destination),
                                PendingIntent.getBroadcast(
                                    context,
                                    notificationParameters.associatedRequestCodes[2],
                                    Intent(
                                        context,
                                        MoveToDefaultDestinationBroadcastReceiver::class.java
                                    )
                                        .putExtra(FileNavigator.EXTRA_MOVE_FILE, moveFile)
                                        .putExtra(
                                            FileNavigator.NotificationParameters.EXTRA,
                                            notificationParameters
                                        )
                                        .putExtra(
                                            FileNavigator.EXTRA_DEFAULT_MOVE_DESTINATION,
                                            defaultMoveDestination
                                        ),
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                                )
                            )
                        )
                    }
                }
                // add delete-file action
                .addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_delete_24,
                        context.getString(R.string.delete),
                        PendingIntent.getBroadcast(
                            context,
                            notificationParameters.associatedRequestCodes[3],
                            Intent(
                                context,
                                FileDeletionBroadcastReceiver::class.java
                            )
                                .putExtra(FileNavigator.EXTRA_MOVE_FILE, moveFile)
                                .putExtra(
                                    FileNavigator.NotificationParameters.EXTRA,
                                    notificationParameters
                                ),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                        )
                    )
                )
        )
    }

    protected abstract fun getNotificationTitleFormatArg(moveFile: MoveFile): String
}

internal fun getFileObservers(
    statusMap: Map<FileType.Status.StoreEntry, FileType.Status>,
    mediaFileSourceEnabled: Map<DataStoreEntry.UniType<Boolean>, Boolean>,
    context: Context,
    getNotificationParameters: (Int) -> FileNavigator.NotificationParameters,
    getDefaultMoveDestination: (FileType.Source) -> Uri?
): List<FileObserver> {
    val mediaFileObservers = FileType.Media.all
        .filterEnabled(statusMap)
        .map { mediaType ->
            MediaFileObserver(
                fileType = mediaType,
                sourceKinds = mediaType
                    .sources
                    .filter { source -> mediaFileSourceEnabled.getValue(source.isEnabled) }
                    .map { source -> source.kind }
                    .toSet(),
                context = context,
                getNotificationParameters = getNotificationParameters,
                getDefaultMoveDestination = getDefaultMoveDestination
            )
        }

    val nonMediaFileObserver =
        FileType.NonMedia.all
            .filterEnabled(statusMap)
            .run {
                if (isNotEmpty()) {
                    NonMediaFileObserver(
                        fileTypes = this,
                        context = context,
                        getNotificationParameters = getNotificationParameters,
                        getDefaultMoveDestination = getDefaultMoveDestination
                    )
                } else {
                    null
                }
            }

    return buildList {
        addAll(mediaFileObservers)
        nonMediaFileObserver?.let {
            add(it)
        }
    }
}