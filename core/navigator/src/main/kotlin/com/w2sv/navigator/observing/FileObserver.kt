package com.w2sv.navigator.observing

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.anggrayudi.storage.media.MediaType
import com.google.common.collect.EvictingQueue
import com.w2sv.common.util.MediaId
import com.w2sv.common.util.log
import com.w2sv.common.util.mediaUri
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.kotlinutils.coroutines.launchDelayed
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.model.DestinationSelectionManner
import com.w2sv.navigator.moving.model.MediaIdWithMediaType
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.NavigatorMoveDestination
import com.w2sv.navigator.notifications.appnotifications.movefile.MoveFileNotificationManager
import com.w2sv.navigator.observing.model.MediaStoreDataProducer
import com.w2sv.navigator.observing.model.MediaStoreFileData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import slimber.log.i

private data class MoveFileWithProcedureJob(val moveFile: MoveFile, val procedureJob: Job)

private const val CANCEL_PERIOD_MILLIS = 300L

private enum class FileChangeOperation(private val flag: Int?) {

    // Note: don't change the enum entry order, as the working of determine(Int): FileChangeOperation depends on it!!!
    Update(ContentResolver.NOTIFY_UPDATE),
    Insert(ContentResolver.NOTIFY_INSERT),
    Delete(ContentResolver.NOTIFY_DELETE),
    Unclassified(null);

    companion object {
        operator fun get(contentObserverOnChangeFlags: Int): FileChangeOperation =
            entries.first { it.flag == null || it.flag and contentObserverOnChangeFlags != 0 }
    }
}

internal abstract class FileObserver(
    val mediaType: MediaType,
    private val context: Context,
    private val moveFileNotificationManager: MoveFileNotificationManager,
    private val mediaStoreDataProducer: MediaStoreDataProducer,
    private val fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
    handler: Handler,
    blacklistedMediaUris: SharedFlow<MediaIdWithMediaType>,
    private val scope: CoroutineScope
) :
    ContentObserver(handler) {

    private val mediaUriBlacklist = EvictingQueue.create<MediaId>(3)
    private var moveFileWithProcedureJob: MoveFileWithProcedureJob? = null

    init {
        blacklistedMediaUris
            .filter { it.mediaType == mediaType }
            .map { it.mediaId }
            .collectOn(scope) { mediaId ->
                i { "Collected $mediaId" }
                mediaUriBlacklist.add(mediaId)
                if (moveFileWithProcedureJob?.moveFile?.mediaUri?.id == mediaId) {
                    cancelAndResetMoveFileProcedureJob()
                }
            }
    }

    protected abstract val logIdentifier: String

    override fun deliverSelfNotifications(): Boolean =
        false

    private fun cancelAndResetMoveFileProcedureJob() {
        moveFileWithProcedureJob?.procedureJob?.cancel()
        moveFileWithProcedureJob = null
    }

    override fun onChange(
        selfChange: Boolean,
        uri: Uri?,
        flags: Int
    ) {
        when (FileChangeOperation[flags].also { emitOnChangeLog(uri, it) }) {
            FileChangeOperation.Insert -> Unit
            FileChangeOperation.Update, FileChangeOperation.Unclassified -> onChangeCore(uri)
            FileChangeOperation.Delete -> cancelAndResetMoveFileProcedureJob()
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        emitOnChangeLog(uri, FileChangeOperation.Unclassified)
        onChangeCore(uri)
    }

    private fun emitOnChangeLog(uri: Uri?, fileChangeOperation: FileChangeOperation) {
        i { "$logIdentifier ${fileChangeOperation.name} $uri | Blacklist: $mediaUriBlacklist" }
    }

    private fun onChangeCore(uri: Uri?) {
        val mediaUri = uri?.mediaUri ?: return

        // Exit if in mediaUriBlacklist
        if (mediaUriBlacklist.contains(mediaUri.id)) {
            i { "Found $mediaUri in blacklist; discarding" }
            return
        }

        val mediaStoreDataRetrievalResult = mediaStoreDataProducer(
            mediaUri = mediaUri,
            contentResolver = context.contentResolver
        )
            .asSuccessOrNull ?: return

        if (mediaStoreDataRetrievalResult.isUpdateOfAlreadySeenFile) {
            moveFileWithProcedureJob?.let {
                if (mediaUri == it.moveFile.mediaUri) {
                    it.procedureJob.cancel()
                }
            }
        }

        enabledFileAndSourceTypeOrNull(mediaStoreDataRetrievalResult.data)
            ?.let { fileAndSourceType ->
                val moveFile = MoveFile(
                    mediaUri = mediaUri,
                    mediaStoreFileData = mediaStoreDataRetrievalResult.data,
                    fileAndSourceType = fileAndSourceType
                )
                    .log { "Calling onMoveFile on $it" }

                val enabledAutoMoveDestination =
                    fileTypeConfigMapStateFlow
                        .value
                        .getValue(moveFile.fileType)
                        .sourceTypeConfigMap
                        .getValue(moveFile.sourceType)
                        .autoMoveConfig
                        .enabledDestinationOrNull

                moveFileWithProcedureJob = MoveFileWithProcedureJob(
                    moveFile = moveFile,
                    procedureJob = scope.launchDelayed(CANCEL_PERIOD_MILLIS) {
                        when (enabledAutoMoveDestination) {
                            null -> {
                                moveFileNotificationManager.buildAndPostNotification(moveFile)
                            }

                            else -> {
                                MoveBroadcastReceiver.sendBroadcast(
                                    moveBundle = MoveBundle.AutoMove(
                                        file = moveFile,
                                        destination = enabledAutoMoveDestination,
                                        destinationSelectionManner = DestinationSelectionManner.Auto
                                    ),
                                    context = context
                                )
                            }
                        }
                    }
                )
            }
    }

    protected abstract fun enabledFileAndSourceTypeOrNull(mediaStoreFileData: MediaStoreFileData): FileAndSourceType?
}

private val AutoMoveConfig.enabledDestinationOrNull: NavigatorMoveDestination.Directory?
    get() = if (enabled && destination != null) NavigatorMoveDestination.Directory(destination!!) else null
