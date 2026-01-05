package com.w2sv.navigator.shared

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent

internal fun mainActivityPendingIntent(context: Context): PendingIntent =
    PendingIntent.getActivity(
        context,
        1,
        mainActivityIntent(context),
        PendingIntent.FLAG_IMMUTABLE
    )

internal fun mainActivityIntent(context: Context): Intent =
    Intent.makeRestartActivityTask(
        ComponentName(context, "com.w2sv.filenavigator.ui.MainActivity")
    )
