package com.w2sv.navigator.observing

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import com.w2sv.common.utils.MediaUri
import com.w2sv.common.utils.cancelIfActive
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.kotlinutils.coroutines.launchDelayed
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.notifications.managers.MoveFileNotificationManager
import com.w2sv.navigator.observing.model.FileChangeOperation
import com.w2sv.navigator.observing.model.MediaStoreData
import com.w2sv.navigator.observing.model.MediaStoreDataProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

private data class MoveFileWithProcedureJob(val moveFile: MoveFile, val procedureJob: Job)

private const val CANCEL_PERIOD_MILLIS = 300L

internal abstract class FileObserver(
    private val context: Context,
    private val moveFileNotificationManager: MoveFileNotificationManager,
    private val mediaStoreDataProducer: MediaStoreDataProducer,
    private val fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
    handler: Handler
) :
    ContentObserver(handler) {

    private val scope = CoroutineScope(Dispatchers.IO)

    protected abstract val logIdentifier: String

    private val fileTypeConfigMap
        get() = fileTypeConfigMapStateFlow.value

    private var moveFileWithProcedureJob: MoveFileWithProcedureJob? = null

    override fun deliverSelfNotifications(): Boolean = false

    override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
        val fileChangeOperation =
            FileChangeOperation.determine(flags).also { emitOnChangeLog(uri, it) }
        when (fileChangeOperation) {
            FileChangeOperation.Delete -> {
                moveFileWithProcedureJob?.procedureJob?.cancelIfActive()
                moveFileWithProcedureJob = null
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

    private fun emitOnChangeLog(uri: Uri?, fileChangeOperation: FileChangeOperation) {
        i { "$logIdentifier ${fileChangeOperation.name} $uri" }
    }

    private fun onChangeCore(uri: Uri?) {
        val mediaUri = uri?.let { MediaUri(it) } ?: return
        val mediaStoreDataRetrievalResult = (mediaStoreDataProducer(
            mediaUri = mediaUri,
            contentResolver = context.contentResolver
        ) as? MediaStoreDataProducer.Result.Success) ?: return

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
                    mediaStoreData = mediaStoreDataRetrievalResult.data,
                    fileAndSourceType = fileAndSourceType
                )
                    .also {
                        i { "Calling onMoveFile on $it" }
                    }

                val enabledAutoMoveDestination =
                    fileTypeConfigMap
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
                                    moveBundle = MoveBundle(
                                        file = moveFile,
                                        destination = enabledAutoMoveDestination,
                                        mode = MoveMode.Auto
                                    ),
                                    context = context,
                                )
                            }
                        }
                    }
                )
            }
    }

    protected abstract fun enabledFileAndSourceTypeOrNull(mediaStoreData: MediaStoreData): FileAndSourceType?
}

private val AutoMoveConfig.enabledDestinationOrNull: MoveDestination?
    get() = if (enabled) destination else null