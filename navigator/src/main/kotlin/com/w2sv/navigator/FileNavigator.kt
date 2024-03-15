package com.w2sv.navigator

import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.services.UnboundService
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.valueUnflowed
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.navigator.fileobservers.FileObserver
import com.w2sv.navigator.fileobservers.getFileObservers
import com.w2sv.navigator.notifications.managers.AppNotificationsManager
import com.w2sv.navigator.notifications.managers.abstrct.AppNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class FileNavigator : UnboundService() {

    @Inject
    lateinit var navigatorRepository: NavigatorRepository

    @Inject
    lateinit var statusChanged: StatusChanged

    @Inject
    lateinit var appNotificationsManager: AppNotificationsManager

    @Inject
    @GlobalScope(AppDispatcher.IO)
    lateinit var ioScope: CoroutineScope

    private lateinit var fileObservers: List<FileObserver>

    private fun getRegisteredFileObservers(): List<FileObserver> =
        getFileObservers(
            fileTypeEnablementMap = navigatorRepository.fileTypeEnablementMap.valueUnflowed(),
            mediaFileSourceEnablementMap = navigatorRepository.mediaFileSourceEnablementMap.valueUnflowed(),
            contentResolver = contentResolver,
            onNewNavigatableFileListener = { moveFile ->
                // with scope because construction of inner class BuilderArgs requires inner class scope
                with(appNotificationsManager.newMoveFileNotificationManager) {
                    buildAndEmit(
                        BuilderArgs(
                            moveFile = moveFile,
                            getLastMoveDestination = { source ->
                                navigatorRepository
                                    .getLastMoveDestinationFlow(source)
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

        return START_STICKY
    }

    private fun start() {
        startForeground(
            1,
            appNotificationsManager.fileNavigatorIsRunningNotificationManager.buildNotification(
                AppNotificationManager.BuilderArgs.Empty
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
    class StatusChanged @Inject constructor(@GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope) {
        val isRunning get() = _isRunning.asSharedFlow()
        private val _isRunning: MutableSharedFlow<Boolean> = MutableSharedFlow()

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
        // Actions
        // ===========

        const val ACTION_REREGISTER_MEDIA_OBSERVERS =
            "com.w2sv.filenavigator.REREGISTER_MEDIA_OBSERVERS"
        const val ACTION_STOP_SERVICE = "com.w2sv.filenavigator.STOP"
    }
}