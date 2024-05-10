package com.w2sv.navigator.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.StringRes
import com.w2sv.core.navigator.R

/**
 * Enum assures required id-uniqueness of resulting [NotificationChannel].
 */
enum class AppNotificationChannel(@StringRes val nameRes: Int) {
    FileNavigatorIsRunning(R.string.file_navigator_is_running),
    NewFile(R.string.new_file)
}

fun AppNotificationChannel.getNotificationChannel(
    context: Context,
    importance: Int = NotificationManager.IMPORTANCE_DEFAULT
): NotificationChannel =
    NotificationChannel(
        name,
        context.getString(nameRes),
        importance
    )