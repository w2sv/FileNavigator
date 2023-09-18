package com.w2sv.navigator

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.coroutines.getSynchronousMap
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.androidutils.notifying.UniqueIds
import com.w2sv.androidutils.notifying.getNotificationManager
import com.w2sv.androidutils.services.UnboundService
import com.w2sv.common.notifications.NotificationChannelProperties
import com.w2sv.common.notifications.createNotificationChannelAndGetNotificationBuilder
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.navigator.fileobservers.FileObserver
import com.w2sv.navigator.fileobservers.getFileObservers
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class FileNavigator : UnboundService() {

    @Inject
    lateinit var fileTypeRepository: FileTypeRepository

    @Inject
    lateinit var statusChanged: StatusChanged

    private val newFileDetectedNotificationIds = UniqueIds(1)
    private val newFileDetectedActionsPendingIntentRequestCodes = UniqueIds(1)

    private lateinit var fileObservers: List<FileObserver>

    private val notificationChannel by lazy {
        NotificationChannelProperties(
            "FileNavigator",
            getString(R.string.file_navigator_is_running)
        )
    }

    private fun setAndRegisterFileObservers(): List<FileObserver> =
        getFileObservers(
            statusMap = fileTypeRepository.fileTypeStatus.getSynchronousMap(),
            mediaFileSourceEnabled = fileTypeRepository.mediaFileSourceEnabled.getSynchronousMap(),
            context = applicationContext,
            getNotificationParameters = {
                NotificationParameters(
                    newFileDetectedNotificationIds.addNewId(),
                    newFileDetectedActionsPendingIntentRequestCodes.addMultipleNewIds(it)
                )
            },
            getDefaultMoveDestination = { fileTypeRepository.getFileSourceDefaultDestinationFlow(it).getValueSynchronously() }
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
                unregisterContentObservers()
                fileObservers = setAndRegisterFileObservers()
            }

            ACTION_CLEANUP_IDS -> {
                val notificationParameters =
                    intent.getParcelableCompat<NotificationParameters>(NotificationParameters.EXTRA)!!

                newFileDetectedNotificationIds.remove(notificationParameters.notificationId)
                newFileDetectedActionsPendingIntentRequestCodes.removeAll(notificationParameters.associatedRequestCodes.toSet())
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
            createNotificationChannelAndGetNotificationBuilder(
                notificationChannel
            )
                .setSmallIcon(R.drawable.ic_file_move_24)
                .setContentTitle(getString(R.string.file_navigator_is_running))
                // add configure action
                .addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_settings_24,
                        getString(R.string.configure),
                        PendingIntent.getActivity(
                            applicationContext,
                            1,
                            Intent.makeRestartActivityTask(
                                ComponentName(this, "com.w2sv.filenavigator.MainActivity")
                            ),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                )
                // add stop action
                .addAction(
                    NotificationCompat.Action(
                        R.drawable.ic_cancel_24,
                        getString(R.string.stop),
                        PendingIntent.getService(
                            applicationContext,
                            0,
                            getStopIntent(applicationContext),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                        ),
                    )
                )
                .build()
        )

        fileObservers = setAndRegisterFileObservers()
        statusChanged.emitNewStatus(true)
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        statusChanged.emitNewStatus(false)
    }

    private fun unregisterContentObservers() {
        fileObservers.forEach {
            contentResolver.unregisterContentObserver(it)
        }
        i { "Unregistered fileObservers" }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterContentObservers()
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

    @Parcelize
    data class NotificationParameters(
        val notificationId: Int,
        val associatedRequestCodes: ArrayList<Int>
    ) : Parcelable {

        fun cancelUnderlyingNotification(
            context: Context
        ) {
            context.getNotificationManager().cancel(notificationId)
            onNotificationCancelled(this, context)
        }

        companion object {
            const val EXTRA = "com.w2sv.filenavigator.extra.NOTIFICATION_PARAMETERS"
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

        fun onNotificationCancelled(
            notificationParameters: NotificationParameters,
            context: Context
        ) {
            context.startService(
                getIntent(context)
                    .setAction(ACTION_CLEANUP_IDS)
                    .putExtra(NotificationParameters.EXTRA, notificationParameters)
            )
        }

        fun reregisterFileObservers(context: Context) {
            context.startService(
                getIntent(context)
                    .setAction(ACTION_REREGISTER_MEDIA_OBSERVERS)
            )
        }

        private fun getStopIntent(context: Context): Intent =
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
        const val ACTION_CLEANUP_IDS = "com.w2sv.filenavigator.CLEANUP_IDS"
        const val ACTION_STOP_SERVICE = "com.w2sv.filenavigator.STOP"
    }
}