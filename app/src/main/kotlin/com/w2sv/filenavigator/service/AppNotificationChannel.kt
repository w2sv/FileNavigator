package com.w2sv.filenavigator.service

import androidx.annotation.StringRes
import com.w2sv.filenavigator.R

enum class AppNotificationChannel(@StringRes val titleRes: Int) {
    STARTED_FOREGROUND_SERVICE(R.string.file_navigator_is_running),
    NEW_FILE_DETECTED(R.string.detected_a)
}