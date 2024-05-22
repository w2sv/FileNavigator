package com.w2sv.navigator.moving

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import com.w2sv.androidutils.coroutines.firstBlocking
import com.w2sv.common.utils.ToastProperties
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.common.utils.showToast
import com.w2sv.core.navigator.R
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.putMoveFileExtra
import com.w2sv.navigator.notifications.putNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
internal class FileMoveActivity : ComponentActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        navigatorRepository: NavigatorRepository
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
            navigatorRepository.getLastMoveDestinationFlow(moveFile.source).firstBlocking()
        }
    }

    private val viewModel by viewModels<ViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (val preemptiveExitReason = viewModel.preemptiveExitReason()) {
            null -> destinationPickerLauncher.launch(viewModel.lastMoveDestination)
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
        // Required for quick move, as it remedies "Failed query: java.lang.SecurityException: Permission Denial: opening provider com.android.externalstorage.ExternalStorageProvider from ProcessRecord{6fc17ee 8097:com.w2sv.filenavigator.debug/u0a753} (pid=8097, uid=10753) requires that you obtain access using ACTION_OPEN_DOCUMENT or related APIs"
        contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        // Build DocumentFile, exit if unsuccessful
        val moveDestinationDocumentFile =
            DocumentFile.fromTreeUri(this, treeUri)
                ?: return terminateActivity(ToastProperties(getString(R.string.couldnt_move_file_internal_error)))

        MoveBroadcastReceiver.startFromFileMoveActivityIntent(
            context = applicationContext,
            fileMoveActivityIntent = intent,
            moveDestinationDocumentUri = moveDestinationDocumentFile.uri
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
                .putNotificationResourcesExtra(notificationResources)
    }
}