package com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.utils.fileName
import com.w2sv.common.utils.getText
import com.w2sv.core.navigator.R

fun Context.showFileSuccessfullyMovedToast(targetDirectory: DocumentFile) {
    showToast(
        getText(
            R.string.moved_file_to,
            "/${targetDirectory.fileName(this@showFileSuccessfullyMovedToast)}"
        )
    )
}