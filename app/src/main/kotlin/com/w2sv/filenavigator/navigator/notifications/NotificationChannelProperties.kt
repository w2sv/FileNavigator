package com.w2sv.filenavigator.navigator.notifications

import androidx.annotation.StringRes
import com.w2sv.filenavigator.R
import com.w2sv.kotlinutils.extensions.nonZeroOrdinal

interface NotificationChannelProperties {
    val name: String
    val idGroupSeed: Int
    val titleRes: Int

    enum class FileNavigator(@StringRes override val titleRes: Int) :
        NotificationChannelProperties {
        StartedForegroundService(R.string.file_navigator_is_running),
        NewFileDetected(R.string.new_file_detected_template);

        override val idGroupSeed by this::nonZeroOrdinal
    }
}