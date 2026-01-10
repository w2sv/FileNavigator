package com.w2sv.navigator.moving.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.coroutineScope
import com.w2sv.androidutils.res.getHtmlFormattedText
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.util.intent
import com.w2sv.composed.core.rememberStyledTextResource
import com.w2sv.core.common.R
import com.w2sv.designsystem.DialogButton
import com.w2sv.designsystem.HighlightedDialogButton
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.domain.moving.MoveFileNotificationData
import com.w2sv.navigator.domain.notifications.NotificationEventHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Gets invoked when the user clicks the 'Delete file' notification action on the 'Move file' notification.
 * Must receive [com.w2sv.navigator.domain.moving.MoveFile] & [com.w2sv.navigator.domain.notifications.CancelNotificationEvent] through its intent.
 *
 * Shows a file deletion confirmation dialog. If the user confirms the deletion,
 * it attempts to carry out the deletion, shows a toast informing about the result, cancels the 'Move file' notification
 * corresponding to the file if it was successful and finishes. If the user declines the deletion or cancels the dialog by
 * clicking outside of its area, it finishes right away.
 */
@AndroidEntryPoint
internal class FileDeletionActivity : DialogHostingActivity() {

    @Inject
    lateinit var notificationEventHandler: NotificationEventHandler

    private val args by threadUnsafeLazy { MoveFileNotificationData(intent) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                DeletionConfirmationDialog(
                    fileName = args.moveFile.mediaStoreData.name,
                    onDismissRequest = { finishAndRemoveTask() },
                    onConfirmation = { launchFileDeletion() })
            }
        }
    }

    private fun launchFileDeletion() {
        lifecycle.coroutineScope.launch {
            val successfullyDeleted = with(Dispatchers.IO) {
                contentResolver.delete(args.moveFile.mediaUri.uri, null) > 0
            }
            with(Dispatchers.Main) {
                if (successfullyDeleted) {
                    showToast(
                        resources.getHtmlFormattedText(
                            R.string.successfully_deleted,
                            args.moveFile.mediaStoreData.name
                        )
                    )
                    notificationEventHandler(args.cancelNotificationEvent)
                } else {
                    showToast(
                        resources.getHtmlFormattedText(
                            R.string.couldn_t_delete,
                            args.moveFile.mediaStoreData.name
                        )
                    )
                }

                finishAndRemoveTask()
            }
        }
    }

    companion object {
        fun intent(args: MoveFileNotificationData, context: Context): Intent =
            intent<FileDeletionActivity>(context)
                .putExtra(MoveFileNotificationData.EXTRA, args)
    }
}

@Composable
private fun DeletionConfirmationDialog(
    fileName: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        icon = { Icon(Icons.Rounded.Warning, null, modifier = Modifier.size(32.dp)) },
        text = {
            Text(
                rememberStyledTextResource(
                    R.string.file_deletion_confirmation_dialog_message,
                    fileName
                )
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = { HighlightedDialogButton(text = stringResource(R.string.yes), onClick = onConfirmation) },
        dismissButton = { DialogButton(stringResource(R.string.no), onClick = onDismissRequest) }
    )
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        DeletionConfirmationDialog("someFile", {}, {})
    }
}
