package com.w2sv.navigator

import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.services.UnboundService
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.navigator.fileobservers.FileObserver
import com.w2sv.navigator.fileobservers.getFileObservers
import com.w2sv.navigator.notifications.appnotificationmanager.AppNotificationManager
import com.w2sv.navigator.notifications.AppNotificationsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class FileNavigator : UnboundService() {

    @Inject
    lateinit var fileTypeRepository: FileTypeRepository

    @Inject
    lateinit var statusChanged: StatusChanged

    @Inject
    lateinit var appNotificationsManager: AppNotificationsManager

    private lateinit var fileObservers: List<FileObserver>

    private fun getRegisteredFileObservers(): List<FileObserver> =
        getFileObservers(
            statusMap = fileTypeRepository.fileTypeStatus.getSynchronousMap(),
            mediaFileSourceEnabled = fileTypeRepository.mediaFileSourceEnabled.getSynchronousMap(),
            contentResolver = contentResolver,
            onNewMoveFile = { moveFile ->
                with(appNotificationsManager.newMoveFileNotificationManager) {
                    buildAndEmit(
                        Args(
                            moveFile = moveFile,
                            getDefaultMoveDestination = { source ->
                                fileTypeRepository.getDefaultDestinationFlow(source)
                                    .getValueSynchronously()
                            }
                        )
                    )
                }
            }
        )
            .onEach {
                contentResolver.registerContentObserver(
                    it.contentObserverUri,
                    true,
                    it
                )
            }
            .also { i { "Registered ${it.size} FileObservers" } }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        i { "onStartCommand | action: ${intent?.action}" }

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stop()
            }

            ACTION_REREGISTER_MEDIA_OBSERVERS -> {
                unregisterFileObservers()
                fileObservers = getRegisteredFileObservers()
            }

            else -> try {
                start()
            } catch (e: RuntimeException) {
                i(e)
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        startForeground(
            1,
            appNotificationsManager.foregroundServiceNotificationManager.buildNotification(
                AppNotificationManager.Args.Empty
            )
        )

        fileObservers = getRegisteredFileObservers()
        statusChanged.emitNewStatus(true)
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        statusChanged.emitNewStatus(false)
    }

    private fun unregisterFileObservers() {
        fileObservers.forEach {
            contentResolver.unregisterContentObserver(it)
        }
        i { "Unregistered fileObservers" }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterFileObservers()
        } catch (e: UninitializedPropertyAccessException) {
            i(e)
        }
    }

    @Singleton
    class StatusChanged @Inject constructor() {
        val isRunning get() = _isRunning.asSharedFlow()
        private val _isRunning: MutableSharedFlow<Boolean> = MutableSharedFlow()

        private val scope = CoroutineScope(Dispatchers.Default)

        internal fun emitNewStatus(isRunning: Boolean) {
            scope.launch {
                _isRunning.emit(isRunning)
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startService(
                getIntent(context)
            )
        }

        fun stop(context: Context) {
            context.startService(
                getStopIntent(context)
            )
        }

        fun reregisterFileObservers(context: Context) {
            context.startService(
                getIntent(context)
                    .setAction(ACTION_REREGISTER_MEDIA_OBSERVERS)
            )
        }

        fun getStopIntent(context: Context): Intent =
            getIntent(context)
                .setAction(ACTION_STOP_SERVICE)

        private fun getIntent(context: Context): Intent =
            Intent(context, FileNavigator::class.java)

        // ===========
        // Extras
        // ===========

        const val EXTRA_MOVE_FILE =
            "com.w2sv.filenavigator.extra.MOVE_FILE"
        const val EXTRA_DEFAULT_MOVE_DESTINATION =
            "com.w2sv.filenavigator.extra.DEFAULT_MOVE_DESTINATION"

        // ===========
        // Actions
        // ===========

        const val ACTION_REREGISTER_MEDIA_OBSERVERS =
            "com.w2sv.filenavigator.REREGISTER_MEDIA_OBSERVERS"
        const val ACTION_STOP_SERVICE = "com.w2sv.filenavigator.STOP"
    }
}