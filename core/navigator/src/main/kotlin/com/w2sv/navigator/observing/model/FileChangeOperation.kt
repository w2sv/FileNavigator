package com.w2sv.navigator.observing.model

import android.content.ContentResolver
import android.os.Build
import androidx.annotation.RequiresApi

internal enum class FileChangeOperation(private val flag: Int?) {
    @RequiresApi(Build.VERSION_CODES.R)
    Update(ContentResolver.NOTIFY_UPDATE),

    @RequiresApi(Build.VERSION_CODES.R)
    Insert(ContentResolver.NOTIFY_INSERT),

    @RequiresApi(Build.VERSION_CODES.R)
    Delete(ContentResolver.NOTIFY_DELETE),

    Unclassified(null);

    companion object {
        /**
         * Depends on [Unclassified] being the last entry!!!
         */
        fun determine(contentObserverOnChangeFlags: Int): FileChangeOperation =
            entries.first { it.flag == null || it.flag and contentObserverOnChangeFlags != 0 }
    }
}