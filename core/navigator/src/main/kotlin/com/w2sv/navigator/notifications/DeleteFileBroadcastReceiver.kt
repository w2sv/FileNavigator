package com.w2sv.navigator.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.widget.showToast
import com.w2sv.navigator.moving.model.MoveFileWithNotificationResources

internal class DeleteFileBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val (moveFile, notificationResources) = MoveFileWithNotificationResources.fromIntent(intent)

        val successfullyDeleted = context.contentResolver.delete(moveFile.mediaUri.uri, null) > 0

        context.showToast(if (successfullyDeleted) "Successfully deleted" else "Couldn't delete")
        notificationResources.cancelNotification(context)
    }

    companion object {
        fun getIntent(moveFileWithNotificationResources: MoveFileWithNotificationResources, context: Context): Intent =
            Intent(context, DeleteFileBroadcastReceiver::class.java)
                .apply { moveFileWithNotificationResources.addToIntent(this) }
    }
}
