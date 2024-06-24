package com.w2sv.navigator

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import com.anggrayudi.storage.media.MediaType
import com.w2sv.androidutils.UnboundService
import com.w2sv.androidutils.isServiceRunning
import com.w2sv.navigator.fileobservers.FileObserver
import com.w2sv.navigator.fileobservers.FileObserverProvider
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.MoveMode
import com.w2sv.navigator.notifications.managers.FileNavigatorIsRunningNotificationManager
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

internal typealias MediaTypeToFileObserver = Map<MediaType, FileObserver>

@AndroidEntryPoint
class FileNavigator : UnboundService() {

    @Inject
    internal lateinit var isRunningStateFlow: IsRunningStateFlow

    @Inject
    internal lateinit var fileNavigatorIsRunningNotificationManager: FileNavigatorIsRunningNotificationManager

    @Inject
    internal lateinit var newMoveFileNotificationManager: NewMoveFileNotificationManager

    @Inject
    internal lateinit var fileObserverProvider: FileObserverProvider

    private var fileObservers: MediaTypeToFileObserver? = null

    private val contentObserverHandlerThread by lazy {
        HandlerThread("com.w2sv.filenavigator.ContentObserverThread")
    }

    private fun getRegisteredFileObservers(): MediaTypeToFileObserver {
        if (!contentObserverHandlerThread.isAlive) {
            contentObserverHandlerThread.start()
        }
        return fileObserverProvider(
            contentResolver = contentResolver,
            onNewMoveFile = { moveFile ->
                when (moveFile.moveMode) {
                    is MoveMode.Auto -> {
                        MoveBroadcastReceiver.sendBroadcast(
                            context = applicationContext,
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
                            )
                        }
                    }
                }
            },
            handler = Handler(contentObserverHandlerThread.looper)
        )
            .onEach { (mediaType, fileObserver) ->
                contentResolver.registerContentObserver(
                    mediaType.readUri!!,
                    true,
                    fileObserver
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

        i { "Registering file observers" }
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
        fileObservers?.values?.forEach {
            contentResolver.unregisterContentObserver(it)
        }
        i { "Unregistered fileObservers" }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterFileObservers()
        contentObserverHandlerThread.quit()
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