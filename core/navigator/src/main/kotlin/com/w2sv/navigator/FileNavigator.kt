package com.w2sv.navigator

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import com.w2sv.androidutils.coroutines.firstBlocking
import com.w2sv.androidutils.services.UnboundService
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.navigator.fileobservers.FileObserver
import com.w2sv.navigator.fileobservers.getFileObservers
import com.w2sv.navigator.notifications.managers.FileNavigatorIsRunningNotificationManager
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class FileNavigator : UnboundService() {

    @Inject
    internal lateinit var navigatorConfigDataSource: NavigatorConfigDataSource

    @Inject
    internal lateinit var isRunningStateFlow: IsRunningStateFlow

    @Inject
    internal lateinit var fileNavigatorIsRunningNotificationManager: FileNavigatorIsRunningNotificationManager

    @Inject
    internal lateinit var newMoveFileNotificationManager: NewMoveFileNotificationManager

    private lateinit var fileObservers: List<FileObserver>

    private val contentObserverHandlerThread =
        HandlerThread("com.w2sv.filenavigator.ContentObserverThread")

    private fun getRegisteredFileObservers(): List<FileObserver> {
        if (!contentObserverHandlerThread.isAlive) {
            contentObserverHandlerThread.start()
        }
        return getFileObservers(
            fileTypeConfigMap = navigatorConfigDataSource.navigatorConfig.map { it.fileTypeConfigMap }
                .firstBlocking(),
            contentResolver = contentResolver,
            onNewNavigatableFileListener = { moveFile ->
                // with scope because construction of inner class BuilderArgs requires inner class scope
                with(newMoveFileNotificationManager) {
                    buildAndEmit(
                        BuilderArgs(
                            moveFile = moveFile
                        )
                    )
                }
            },
            handler = Handler(contentObserverHandlerThread.looper)
        )
            .onEach {
                contentResolver.registerContentObserver(
                    it.contentObserverUri,
                    true,
                    it
                )
            }
            .also { i { "Registered ${it.size} FileObserver(s)" } }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        i { "onStartCommand | action: ${intent?.action}" }

        when (intent?.action) {
            Action.STOP_SERVICE -> {
                stop()
            }

            Action.REREGISTER_MEDIA_OBSERVERS -> {
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

        return START_STICKY
    }

    private fun start() {
        startForeground(
            1,
            fileNavigatorIsRunningNotificationManager.buildNotification(
                AppNotificationManager.BuilderArgs.Empty
            )
        )

        fileObservers = getRegisteredFileObservers()
        isRunningStateFlow.value = true
    }

    private fun stop() {
        i { "FileNavigator.stop" }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        isRunningStateFlow.value = false
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
        } finally {
            contentObserverHandlerThread.quit()
        }
    }

    @Singleton
    class IsRunningStateFlow @Inject constructor(
        @ApplicationContext context: Context
    ) : MutableStateFlow<Boolean> by MutableStateFlow(context.isServiceRunning<FileNavigator>())

    private data object Action {
        const val REREGISTER_MEDIA_OBSERVERS =
            "com.w2sv.filenavigator.REREGISTER_MEDIA_OBSERVERS"
        const val STOP_SERVICE = "com.w2sv.filenavigator.STOP"
    }

    companion object {
        fun start(context: Context) {
            context.startForegroundService(
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
                    .setAction(Action.REREGISTER_MEDIA_OBSERVERS)
            )
        }

        fun getStopIntent(context: Context): Intent =
            getIntent(context)
                .setAction(Action.STOP_SERVICE)

        private fun getIntent(context: Context): Intent =
            Intent(context, FileNavigator::class.java)
    }
}