package com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import com.anggrayudi.storage.media.MediaFile
import com.w2sv.androidutils.coroutines.firstBlocking
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.utils.ToastArgs
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.core.navigator.R
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.navigator.model.MoveFile
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.newmovefile.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors.receivers.MoveBroadcastReceiver
import com.w2sv.navigator.notifications.putMoveDestinationExtra
import com.w2sv.navigator.notifications.putMoveFileExtra
import com.w2sv.navigator.notifications.putNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
internal class FileMoveActivity : ComponentActivity() {

    internal enum class PreemptiveExitReason {
        MissingManageAllFilesPermission,
        MoveMediaFileNull,
        MoveFileNotFound
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        navigatorRepository: NavigatorRepository,
        @ApplicationContext context: Context
    ) :
        androidx.lifecycle.ViewModel() {

        val moveFile: MoveFile =
            savedStateHandle[MoveFile.EXTRA]!!

        val moveMediaFile: MediaFile? = moveFile.getSimpleStorageMediaFile(context)

        internal fun preemptiveExitReason(): PreemptiveExitReason? =
            when {
                !isExternalStorageManger() -> PreemptiveExitReason.MissingManageAllFilesPermission
                !moveFile.mediaStoreFile.columnData.fileExists -> PreemptiveExitReason.MoveFileNotFound
                moveMediaFile == null -> PreemptiveExitReason.MoveMediaFileNull
                else -> null
            }

        val lastMoveDestination =
            navigatorRepository.getLastMoveDestinationFlow(moveFile.source).firstBlocking()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (viewModel.preemptiveExitReason().also { i { "Preemptive exit reason: $it" } }) {
            PreemptiveExitReason.MissingManageAllFilesPermission -> showResultToastAndFinishActivity(
                ToastArgs(
                    getString(R.string.couldnt_move_manage_all_files_permission_missing)
                )
            )

            PreemptiveExitReason.MoveMediaFileNull -> showResultToastAndFinishActivity(
                ToastArgs(
                    getString(R.string.couldnt_move_file_internal_error)
                )
            )

            PreemptiveExitReason.MoveFileNotFound -> {
                removeNotificationAndCleanupResources()
                showResultToastAndFinishActivity(
                    ToastArgs(
                        getString(R.string.couldn_t_find_move_file),
                        Toast.LENGTH_LONG
                    )
                )
            }

            null -> destinationPickerLauncher.launch(viewModel.lastMoveDestination)
        }
    }

    private val viewModel by viewModels<ViewModel>()

    private val destinationPickerLauncher =
        registerForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
            callback = ::onTargetDirSelected
        )

    private fun onTargetDirSelected(treeUri: Uri?) {
        i { "Move destination DocumentTree Uri: $treeUri" }

        // Exit on null treeUri (received on exiting folder picker via back press)
        treeUri ?: return showResultToastAndFinishActivity()

        // Take persistable read & write permission
        // Required for quick move, as it remedies "Failed query: java.lang.SecurityException: Permission Denial: opening provider com.android.externalstorage.ExternalStorageProvider from ProcessRecord{6fc17ee 8097:com.w2sv.filenavigator.debug/u0a753} (pid=8097, uid=10753) requires that you obtain access using ACTION_OPEN_DOCUMENT or related APIs"
        contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        // Exit on unsuccessful conversion to DocumentFile.
        val targetDirectoryDocumentFile =
            DocumentFile.fromTreeUri(this, treeUri)
                ?: return showResultToastAndFinishActivity(ToastArgs(getString(R.string.couldnt_move_file_internal_error)))

        // Exit if file already at selected location.
        if (targetDirectoryDocumentFile.hasChild(
                context = this,
                path = viewModel.moveFile.mediaStoreFile.columnData.name,
                requiresWriteAccess = false
            )
        ) {
            return showResultToastAndFinishActivity(ToastArgs(getString(R.string.file_already_at_selected_location)))
        }

        sendBroadcast(
            intent.apply {
                setClass(applicationContext, MoveBroadcastReceiver::class.java)
                putMoveDestinationExtra(targetDirectoryDocumentFile.uri)
            }
        )
        finishAndRemoveTask()
    }

    private fun removeNotificationAndCleanupResources() {
        NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
            applicationContext,
            intent
        )
    }

    private fun showResultToastAndFinishActivity(toastArgs: ToastArgs? = null) {
        toastArgs?.run {
            showToast(message, duration)
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