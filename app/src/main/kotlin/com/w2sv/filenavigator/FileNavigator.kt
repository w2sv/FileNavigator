package com.w2sv.filenavigator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.notifying.getNotificationManager
import com.w2sv.androidutils.notifying.showNotification
import com.w2sv.kotlinutils.extensions.nonZeroOrdinal
import slimber.log.i

class FileNavigator : Service() {

    companion object {
        fun startService(context: Context) {
            context.startService(
                Intent(context, FileNavigator::class.java)
            )
            i { "Starting FileNavigator" }
        }

        fun stopService(context: Context) {
            context.startService(
                Intent(context, FileNavigator::class.java)
                    .setAction(ACTION_STOP_SERVICE)
            )
            i { "Stopping FileNavigator" }
        }

        private const val ACTION_STOP_SERVICE = "com.w2sv.filetrafficnavigator.STOP"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            AppNotificationChannel.STARTED_FOREGROUND_SERVICE.nonZeroOrdinal,
            applicationContext.createNotificationChannelAndGetNotificationBuilder(
                AppNotificationChannel.STARTED_FOREGROUND_SERVICE,
                AppNotificationChannel.STARTED_FOREGROUND_SERVICE.title
            )
                .setContentText("Waiting for new files to be navigated...")
                .build()
        )

        MediaType.values().forEach {
            contentResolver.registerContentObserver(
                it.mediaStoreUri,
                true,
                mediaObserver
            )
            i { "Registered mediaObserver for ${it.name} files" }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private val mediaObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)

            i { "Registered new uri" }

            showNotification(
                AppNotificationChannel.NEW_FILE_DETECTED.nonZeroOrdinal,
                createNotificationChannelAndGetNotificationBuilder(
                    AppNotificationChannel.NEW_FILE_DETECTED,
                    AppNotificationChannel.NEW_FILE_DETECTED.title
                )
                    .setContentText(uri!!.toString())
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        contentResolver.unregisterContentObserver(mediaObserver)
        i { "Unregistered mediaObserver" }
    }
}

private fun Context.createNotificationChannelAndGetNotificationBuilder(
    channel: AppNotificationChannel,
    contentTitle: String? = null
): NotificationCompat.Builder {
    getNotificationManager().createNotificationChannel(
        NotificationChannel(
            channel.name,
            channel.title,
            NotificationManager.IMPORTANCE_DEFAULT
        )
    )
    return notificationBuilder(channel.name, contentTitle)
}

private fun Context.notificationBuilder(
    channelId: String,
    title: String?,
): NotificationCompat.Builder =
    NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.ic_file_move_24)
        .setContentTitle(title)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)