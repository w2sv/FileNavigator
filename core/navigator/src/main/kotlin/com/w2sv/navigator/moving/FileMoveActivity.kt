package com.w2sv.navigator.moving

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateHandle
import com.w2sv.androidutils.coroutines.firstBlocking
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.ToastProperties
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.common.utils.showToast
import com.w2sv.common.utils.takePersistableReadAndWriteUriPermission
import com.w2sv.core.navigator.R
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.putMoveFileExtra
import com.w2sv.navigator.notifications.putOptionalNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
internal class FileMoveActivity : ComponentActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        navigatorConfigDataSource: NavigatorConfigDataSource
    ) :
        androidx.lifecycle.ViewModel() {

        private val moveFile: MoveFile =
            savedStateHandle[MoveFile.EXTRA]!!

        fun preemptiveExitReason(): MoveException? =
            when {
                !isExternalStorageManger -> MoveException.MissingManageAllFilesPermission
                !moveFile.mediaStoreFile.columnData.fileExists -> MoveException.MoveFileNotFound
                else -> null
            }

        val lastMoveDestination by lazy {
            navigatorConfigDataSource.lastMoveDestination(moveFile.fileType, moveFile.sourceType)
                .firstBlocking()
                .firstOrNull()
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (val preemptiveExitReason = viewModel.preemptiveExitReason()) {
            null -> destinationPickerLauncher.launch(viewModel.lastMoveDestination?.uri)
            else -> terminateActivity(
                toastProperties = preemptiveExitReason.toastProperties,
                cancelNotification = preemptiveExitReason.cancelNotification
            )
        }
    }

    private val destinationPickerLauncher =
        registerForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
            callback = { treeUri ->
                if (treeUri == null) {  // When file picker exited via back press
                    terminateActivity()
                } else {
                    onMoveDestinationSelected(treeUri)
                }
            }
        )

    private fun onMoveDestinationSelected(treeUri: Uri) {
        i { "Move destination treeUri: $treeUri" }

        // Take persistable read & write permission
        // Required for quick move
        contentResolver.takePersistableReadAndWriteUriPermission(treeUri)

        // Build DocumentUri, exit if unsuccessful
        val moveDestinationDocumentUri =
            DocumentUri.fromTreeUri(this, treeUri)
                ?: return terminateActivity(ToastProperties(getString(R.string.couldnt_move_file_internal_error)))

        MoveBroadcastReceiver.sendBroadcast(
            context = applicationContext,
            fileMoveActivityIntent = intent.putMoveFileExtra(
                MoveFile.fromIntent(intent)
                    .copy(moveMode = MoveMode.Manual(moveDestinationDocumentUri))
            ),
        )
        terminateActivity()
    }

    private fun terminateActivity(
        toastProperties: ToastProperties? = null,
        cancelNotification: Boolean = false
    ) {
        if (cancelNotification) {
            NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
                applicationContext,
                intent
            )
        }
        toastProperties?.let {
            showToast(it)
        }
        finishAndRemoveTask()
    }

    companion object {
        fun makeRestartActivityIntent(
            moveFile: MoveFile,
            notificationResources: NotificationResources,
            context: Context
        ): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    FileMoveActivity::class.java
                )
            )
                .putMoveFileExtra(moveFile)
                .putOptionalNotificationResourcesExtra(notificationResources)
    }
}