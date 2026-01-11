package com.w2sv.navigator

import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.content.intent
import com.w2sv.common.logging.LoggingUnboundService
import com.w2sv.common.util.hasManageAllFilesPermission
import com.w2sv.common.util.hasPostNotificationsPermission
import com.w2sv.navigator.domain.notifications.ForegroundNotificationProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i
import slimber.log.w

@AndroidEntryPoint
class FileNavigator : LoggingUnboundService() {

    @Inject
    internal lateinit var isRunning: IsRunning

    @Inject
    internal lateinit var foregroundNotificationProvider: ForegroundNotificationProvider

    @Inject
    internal lateinit var fileNavigationLauncher: FileNavigationLauncher

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logOnStartCommand(intent)

        when (val action = intent?.action) {
            Action.START -> start()
            Action.STOP -> stop()
            Action.REREGISTER_FILE_OBSERVERS -> fileNavigationLauncher.reregisterFileObservers()
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

        fileNavigationLauncher.launch()
        isRunning.set(true)
    }

    private fun stop() {
        i { "Stopping FileNavigator" }
        isRunning.set(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        fileNavigationLauncher.tearDown()
        isRunning.set(false)
    }

    class IsRunning internal constructor(private val mutableStateFlow: MutableStateFlow<Boolean>) :
        StateFlow<Boolean> by mutableStateFlow {

        internal fun set(value: Boolean) {
            mutableStateFlow.value = value
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
