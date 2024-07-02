package com.w2sv.navigator

import android.content.Context
import android.content.Intent
import com.anggrayudi.storage.media.MediaType
import com.w2sv.androidutils.UnboundService
import com.w2sv.navigator.notifications.managers.FileNavigatorIsRunningNotificationManager
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import com.w2sv.navigator.observing.FileObserver
import com.w2sv.navigator.observing.FileObserverFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i
import javax.inject.Inject

internal typealias MediaTypeToFileObserver = Map<MediaType, FileObserver>

@AndroidEntryPoint
class FileNavigator : UnboundService() {

    @Inject
    internal lateinit var isRunning: IsRunning

    @Inject
    internal lateinit var isRunningNotificationManager: FileNavigatorIsRunningNotificationManager

    @Inject
    internal lateinit var fileObserverFactory: FileObserverFactory

    private var mediaTypeToFileObserver: MediaTypeToFileObserver? = null

    private fun getRegisteredFileObservers(): MediaTypeToFileObserver {
        return fileObserverFactory.invoke()
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
                mediaTypeToFileObserver = getRegisteredFileObservers()
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
            isRunningNotificationManager.buildNotification(
                AppNotificationManager.BuilderArgs.Empty
            )
        )

        i { "Registering file observers" }
        mediaTypeToFileObserver = getRegisteredFileObservers()
        isRunning.setState(true)
    }

    private fun stop() {
        i { "FileNavigator.stop" }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        isRunning.setState(false)
    }

    private fun unregisterFileObservers() {
        mediaTypeToFileObserver?.values?.forEach {
            contentResolver.unregisterContentObserver(it)
        }
        i { "Unregistered fileObservers" }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterFileObservers()
        fileObserverFactory.quitThread()
    }

    class IsRunning internal constructor(private val mutableStateFlow: MutableStateFlow<Boolean>) :
        StateFlow<Boolean> by mutableStateFlow {

        internal fun setState(value: Boolean) {
            mutableStateFlow.value = value
        }
    }

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