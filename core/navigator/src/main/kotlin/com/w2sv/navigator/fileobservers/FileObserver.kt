package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.w2sv.common.utils.MediaUri
import com.w2sv.kotlinutils.time.durationToNow
import com.w2sv.navigator.mediastore.MediaStoreFile
import com.w2sv.navigator.mediastore.MediaStoreFileRetriever
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.MoveFile
import com.w2sv.navigator.moving.MoveMode
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import slimber.log.i
import java.time.LocalDateTime

private enum class FileChangeOperation(private val flag: Int?) {
    @RequiresApi(Build.VERSION_CODES.R)
    Update(ContentResolver.NOTIFY_UPDATE),

    @RequiresApi(Build.VERSION_CODES.R)
    Insert(ContentResolver.NOTIFY_INSERT),

    @RequiresApi(Build.VERSION_CODES.R)
    Delete(ContentResolver.NOTIFY_DELETE),

    Unclassified(null);

    companion object {
        /**
         * Depends on [Unclassified] being the last entry!!!
         */
        fun determine(contentObserverOnChangeFlags: Int): FileChangeOperation =
            entries.first { it.flag == null || it.flag and contentObserverOnChangeFlags != 0 }
    }
}

internal abstract class FileObserver(
    private val context: Context,
    private val newMoveFileNotificationManager: NewMoveFileNotificationManager,
    private val mediaStoreFileRetriever: MediaStoreFileRetriever,
    handler: Handler
) :
    ContentObserver(handler) {

    protected abstract val logIdentifier: String

    override fun deliverSelfNotifications(): Boolean = false

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
        val fileChangeOperation =
            FileChangeOperation.determine(flags).also { emitOnChangeLog(uri, it) }
        when (fileChangeOperation) {
            FileChangeOperation.Delete -> {
                if (mostRecentEmittedNotification?.dateTime?.durationToNow()
                        ?.toMillis()?.let { it <= 500 } == true
                ) {
                    newMoveFileNotificationManager.cancelNotificationAndFreeResources(
                        mostRecentEmittedNotification!!.builderArgs.resources
                    )
                }
            }

            FileChangeOperation.Insert -> Unit
            FileChangeOperation.Update, FileChangeOperation.Unclassified -> {
                onChangeCore(uri)
            }
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        emitOnChangeLog(uri, FileChangeOperation.Unclassified)
        onChangeCore(uri)
    }

    private data class EmittedNotification(
        val builderArgs: NewMoveFileNotificationManager.BuilderArgs,
        val dateTime: LocalDateTime = LocalDateTime.now()
    )

    private var mostRecentEmittedNotification: EmittedNotification? = null

    private fun onNewMoveFile(moveFile: MoveFile) {
        when (moveFile.moveMode) {
            is MoveMode.Auto -> {
                MoveBroadcastReceiver.sendBroadcast(
                    context = context,
                    moveFile = moveFile
                )
            }

            else -> {
                // with scope because construction of inner class BuilderArgs requires inner class scope
                with(newMoveFileNotificationManager) {
                    buildAndEmit(
                        BuilderArgs(
                            moveFile = moveFile
                        )
                            .also {
                                mostRecentEmittedNotification = EmittedNotification(it)
                            }
                    )
                }
            }
        }
    }

    private fun emitOnChangeLog(uri: Uri?, fileChangeOperation: FileChangeOperation) {
        i { "$logIdentifier ${fileChangeOperation.name} $uri" }
    }

    private fun onChangeCore(uri: Uri?) {
        val mediaUri = uri?.let { MediaUri(it) } ?: return

        val mediaStoreFileRetrievalResult =
            mediaStoreFileRetriever.provide(
                mediaUri = mediaUri,
                contentResolver = context.contentResolver
            )

        if (mediaStoreFileRetrievalResult !is MediaStoreFileRetriever.Result.Success) return

        getMoveFileIfMatchingConstraints(mediaStoreFileRetrievalResult.mediaStoreFile)
            ?.let {
                i { "Calling onNewNavigatableFileListener on $it" }
                onNewMoveFile(it)
            }
    }

    protected abstract fun getMoveFileIfMatchingConstraints(
        mediaStoreFile: MediaStoreFile
    ): MoveFile?
}