package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
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

internal abstract class FileObserver(
    private val context: Context,
    private val newMoveFileNotificationManager: NewMoveFileNotificationManager,
    private val mediaStoreFileRetriever: MediaStoreFileRetriever,
    handler: Handler
) :
    ContentObserver(handler) {

    protected abstract val logIdentifier: String

    override fun deliverSelfNotifications(): Boolean = false

    override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
        if (flags and ContentResolver.NOTIFY_UPDATE != 0) {
            emitOnChangeLog(uri, "UPDATE")
            onChangeCore(uri)
        }
        if (flags and ContentResolver.NOTIFY_INSERT != 0) {
            emitOnChangeLog(uri, "INSERT")
        }
        if (flags and ContentResolver.NOTIFY_DELETE != 0 && mostRecentEmittedNotification?.dateTime?.durationToNow()
                ?.toMillis()?.let { it <= 1_000 } == true
        ) {
            emitOnChangeLog(uri, "DELETE")
            newMoveFileNotificationManager.cancelNotificationAndFreeResources(
                mostRecentEmittedNotification!!.builderArgs.resources
            )
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        emitOnChangeLog(uri, "FLAGLESS")
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

    private fun emitOnChangeLog(uri: Uri?, flagTitle: String) {
        i { "$logIdentifier $flagTitle $uri" }
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