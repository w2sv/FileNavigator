package com.w2sv.navigator.fileobservers

import android.content.ContentResolver
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
import com.w2sv.navigator.mediastore.MediaStoreFileProducer
import com.w2sv.navigator.mediastore.MoveFile
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.MoveBundle
import com.w2sv.navigator.moving.MoveMode
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

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
    private val mediaStoreFileProducer: MediaStoreFileProducer,
    private val fileTypeConfigMapStateFlow: StateFlow<FileTypeConfigMap>,
    handler: Handler
) :
    ContentObserver(handler) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    protected abstract val logIdentifier: String

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
        val mediaStoreFile =
            mediaStoreFileProducer.mediaStoreFileOrNull(mediaUri, context.contentResolver) ?: return

        enabledFileAndSourceTypeOrNull(mediaStoreFile)
            ?.let { fileAndSourceType ->
                onMoveBundle(
                    MoveBundle(
                        moveFile = mediaStoreFile,
                        fileAndSourceType = fileAndSourceType,
                        moveMode = fileTypeConfigMapStateFlow.value.getValue(fileAndSourceType.fileType).sourceTypeConfigMap.getValue(
                            fileAndSourceType.sourceType
                        )
                            .autoMoveConfig
                            .moveMode
                    )
                        .also {
                            i { "Calling onMoveBundle on $it" }
                        }
                )
            }
    }

    protected abstract fun enabledFileAndSourceTypeOrNull(
        moveFile: MoveFile
    ): FileAndSourceType?

    private var mostRecentMoveBundleProcedureJob: Job? = null

    private fun onMoveBundle(moveBundle: MoveBundle) {
        mostRecentMoveBundleProcedureJob = scope.launchDelayed(300L) {
            when (moveBundle.moveMode) {
                is MoveMode.Auto -> {
                    MoveBroadcastReceiver.sendBroadcast(
                        context = context,
                        moveBundle = moveBundle
                    )
                }

                else -> {
                    // with scope because construction of inner class BuilderArgs requires inner class scope
                    with(newMoveFileNotificationManager) {
                        buildAndEmit(
                            BuilderArgs(
                                moveBundle = moveBundle
                            )
                        )
                    }
                }
            }
        }
    }
}

private val AutoMoveConfig.moveMode: MoveMode?
    get() = enabledDestination
        ?.let { MoveMode.Auto(it) }

private val AutoMoveConfig.enabledDestination: DocumentUri?
    get() = if (enabled) destination else null