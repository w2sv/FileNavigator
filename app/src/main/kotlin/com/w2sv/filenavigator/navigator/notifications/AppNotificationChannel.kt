package com.w2sv.filenavigator.navigator.notifications

import androidx.annotation.StringRes
import com.w2sv.filenavigator.R
import com.w2sv.kotlinutils.extensions.nonZeroOrdinal

enum class AppNotificationChannel(@StringRes val titleRes: Int) {
    StartedForegroundService(R.string.file_navigator_is_running),
    NewFileDetected(R.string.new_file_detected_template);

    val idGroupSeed by this::nonZeroOrdinal
}