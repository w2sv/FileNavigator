package com.w2sv.navigator

import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.content.intent
import com.w2sv.core.logging.LoggingUnboundService
import com.w2sv.core.util.hasManageAllFilesPermission
import com.w2sv.core.util.hasPostNotificationsPermission
import com.w2sv.navigator.domain.notifications.ForegroundNotificationProvider
import com.w2sv.navigator.observing.FileObserverManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import slimber.log.i
import slimber.log.w

@AndroidEntryPoint
class FileNavigator : LoggingUnboundService() {

    @Inject
    internal lateinit var status: Status

    @Inject
    internal lateinit var foregroundNotificationProvider: ForegroundNotificationProvider

    @Inject
    internal lateinit var fileObserverManager: FileObserverManager

    @Inject
    internal lateinit var moveResultCollector: MoveResultCollector

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var moveResultCollectionJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logOnStartCommand(intent)

        when (val action = intent?.action) {
            Action.START -> start()
            Action.STOP -> stop()
            Action.REREGISTER_FILE_OBSERVERS -> serviceScope.launch { fileObserverManager.reregisterFileObservers() }
            else -> w { "Service started with unknown action: $action" }
        }

        return START_STICKY
    }

    private fun start() {
        i { "Starting FileNavigator" }
        startForeground(
            foregroundNotificationProvider.notificationId,
            foregroundNotificationProvider.notification()
        )

        serviceScope.launch {
            fileObserverManager.registerFileObservers()
            moveResultCollectionJob?.cancel()
            moveResultCollectionJob = launch { moveResultCollector.startCollecting() }
        }
        status.setIsRunning(true)
    }

    private fun stop() {
        i { "Stopping FileNavigator" }
        status.setIsRunning(false)
        moveResultCollectionJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        fileObserverManager.tearDown()
        serviceScope.cancel()
        status.setIsRunning(false)
    }

    @Singleton
    internal class Status @Inject constructor() {

        val isRunning: StateFlow<Boolean>
            field = MutableStateFlow(false)

        fun setIsRunning(value: Boolean) {
            isRunning.value = value
        }
    }

    private data object Action {
        const val START = "com.w2sv.filenavigator.START_NAVIGATOR"
        const val STOP = "com.w2sv.filenavigator.STOP_NAVIGATOR"
        const val REREGISTER_FILE_OBSERVERS = "com.w2sv.filenavigator.REREGISTER_MEDIA_OBSERVERS"
    }

    companion object {
        fun start(context: Context) {
            context.startForegroundService(
                intent<FileNavigator>(context)
                    .setAction(Action.START)
            )
        }

        fun stop(context: Context) {
            context.startService(stopIntent(context))
        }

        fun reregisterFileObservers(context: Context) {
            context.startService(
                intent<FileNavigator>(context)
                    .setAction(Action.REREGISTER_FILE_OBSERVERS)
            )
        }

        fun stopIntent(context: Context): Intent =
            intent<FileNavigator>(context)
                .setAction(Action.STOP)

        /**
         * @return whether the necessary permissions for running the [FileNavigator] are granted.
         */
        fun necessaryPermissionsGranted(context: Context): Boolean =
            hasManageAllFilesPermission && context.hasPostNotificationsPermission()
    }
}
