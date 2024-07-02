package com.w2sv.navigator.observing

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.MediaUri
import com.w2sv.domain.model.FileAndSourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.kotlinutils.coroutines.launchDelayed
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import com.w2sv.navigator.observing.model.FileChangeOperation
import com.w2sv.navigator.observing.model.MediaStoreData
import com.w2sv.navigator.observing.model.MediaStoreDataProducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

internal abstract class FileObserver(
    private val context: Context,
    private val newMoveFileNotificationManager: NewMoveFileNotificationManager,
    private val mediaStoreDataProducer: MediaStoreDataProducer,
    private val fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
    handler: Handler
) :
    ContentObserver(handler) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    protected abstract val logIdentifier: String

    private val fileTypeConfigMap
        get() = fileTypeConfigMapStateFlow.value

    override fun deliverSelfNotifications(): Boolean = false

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
        val fileChangeOperation =
            FileChangeOperation.determine(flags).also { emitOnChangeLog(uri, it) }
        when (fileChangeOperation) {
            FileChangeOperation.Delete -> {
                mostRecentMoveBundleProcedureJob?.let {
                    if (it.isActive) {
                        it.cancel()
                    }
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

    private fun emitOnChangeLog(uri: Uri?, fileChangeOperation: FileChangeOperation) {
        i { "$logIdentifier ${fileChangeOperation.name} $uri" }
    }

    private fun onChangeCore(uri: Uri?) {
        val mediaUri = uri?.let { MediaUri(it) } ?: return
        val mediaStoreData =
            mediaStoreDataProducer.mediaStoreDataOrNull(mediaUri, context.contentResolver) ?: return

        enabledFileAndSourceTypeOrNull(mediaStoreData)
            ?.let { fileAndSourceType ->
                onMoveFile(
                    MoveFile(
                        mediaUri = mediaUri,
                        mediaStoreData = mediaStoreData,
                        fileAndSourceType = fileAndSourceType
                    )
                        .also {
                            i { "Calling onMoveFile on $it" }
                        }
                )
            }
    }

    protected abstract fun enabledFileAndSourceTypeOrNull(mediaStoreData: MediaStoreData): FileAndSourceType?

    private var mostRecentMoveBundleProcedureJob: Job? = null

    private fun onMoveFile(moveFile: MoveFile) {
        val enabledAutoMoveDestination =
            fileTypeConfigMap
                .getValue(moveFile.fileType)
                .sourceTypeConfigMap
                .getValue(moveFile.sourceType)
                .autoMoveConfig
                .enabledDestinationOrNull

        mostRecentMoveBundleProcedureJob = scope.launchDelayed(300L) {
            when (enabledAutoMoveDestination) {
                null -> {
                    // with scope because construction of inner class BuilderArgs requires inner class scope
                    with(newMoveFileNotificationManager) {
                        buildAndEmit(
                            BuilderArgs(
                                moveFile = moveFile
                            )
                        )
                    }
                }

                else -> {
                    MoveBroadcastReceiver.sendBroadcast(
                        context = context,
                        moveBundle = MoveBundle(
                            file = moveFile,
                            destination = enabledAutoMoveDestination,
                            mode = MoveMode.Auto
                        )
                    )
                }
            }
        }
    }
}

private val AutoMoveConfig.enabledDestinationOrNull: DocumentUri?
    get() = if (enabled) destination else null