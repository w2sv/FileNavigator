package com.w2sv.navigator.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.StringRes
import com.w2sv.core.navigator.R

/**
 * Enum assures required id-uniqueness of resulting [NotificationChannel].
 */
internal enum class AppNotificationChannel(@StringRes val nameRes: Int) {
    FileNavigatorIsRunning(R.string.file_navigator_is_running),
    NewNavigatableFile(R.string.new_navigatable_file),
    AutoMoveDestinationInvalid(R.string.auto_move_destination_invalid),
    MoveProgress(R.string.move_progress);

    fun getNotificationChannel(context: Context, importance: Int = NotificationManager.IMPORTANCE_DEFAULT): NotificationChannel =
        NotificationChannel(
            name,
            context.getString(nameRes),
            importance
        )
}
