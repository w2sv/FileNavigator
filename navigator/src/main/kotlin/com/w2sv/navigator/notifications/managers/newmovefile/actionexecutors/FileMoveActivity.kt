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
import androidx.lifecycle.viewModelScope
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.getSimplePath
import com.anggrayudi.storage.media.MediaFile
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.utils.ToastArgs
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.domain.repository.NavigatorRepository
import com.w2sv.domain.usecase.InsertMoveEntryUseCase
import com.w2sv.navigator.R
import com.w2sv.navigator.model.MoveFile
import com.w2sv.navigator.model.getMoveEntry
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.newmovefile.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.putNavigatableFileExtra
import com.w2sv.navigator.notifications.putNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import slimber.log.i
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class FileMoveActivity : ComponentActivity() {

    internal enum class PreemptiveExitReason {
        MissingManageAllFilesPermission,
        MoveMediaFileNull,
        MoveFileNotFound
    }

    @HiltViewModel
    internal class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        private val navigatorRepository: NavigatorRepository,
        private val insertMoveEntryUseCase: InsertMoveEntryUseCase,
        @ApplicationContext context: Context
    ) :
        androidx.lifecycle.ViewModel() {

        val moveFile: MoveFile =
            savedStateHandle[MoveFile.EXTRA]!!

        val moveMediaFile: MediaFile? = moveFile.getSimpleStorageMediaFile(context)

        fun preemptiveExitReason(): PreemptiveExitReason? =
            when {
                !isExternalStorageManger() -> PreemptiveExitReason.MissingManageAllFilesPermission
                !moveFile.mediaStoreFile.columnData.fileExists -> PreemptiveExitReason.MoveFileNotFound
                moveMediaFile == null -> PreemptiveExitReason.MoveMediaFileNull
                else -> null
            }

        // ===============
        // FileTypeRepository
        // ===============

        fun launchMoveEntryInsertion(destination: Uri, dateTime: LocalDateTime): Job =
            viewModelScope.launch {
                insertMoveEntryUseCase.invoke(getMoveEntry(moveFile, destination, dateTime))
            }

        fun launchLastMoveDestinationSaving(destination: Uri): Job =
            viewModelScope.launch {
                navigatorRepository.saveLastMoveDestination(
                    moveFile.source,
                    destination
                )
            }

        val lastMoveDestination =
            navigatorRepository.getLastMoveDestinationFlow(moveFile.source).getValueSynchronously()
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
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree(), ::onTargetDirSelected)

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

        // Move file
        viewModel.viewModelScope
            .launch {
                viewModel.moveMediaFile!!.moveTo(
                    targetFolder = targetDirectoryDocumentFile,
                    callback = object : FileCallback() {
                        override fun onCompleted(result: Any) {
                            i { "onCompleted" }
                            viewModel
                                .launchLastMoveDestinationSaving(targetDirectoryDocumentFile.uri)

                            viewModel.launchMoveEntryInsertion(
                                targetDirectoryDocumentFile.uri,
                                LocalDateTime.now()
                            )

                            removeNotificationAndCleanupResources()

                            showToast(
                                getString(
                                    R.string.moved_file_to,
                                    targetDirectoryDocumentFile.getSimplePath(applicationContext)
                                )
                            )
                        }

                        override fun onFailed(errorCode: ErrorCode) {
                            i { errorCode.toString() }
                            showToast(errorCode.name)
                        }
                    }
                )
            }

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
                .putNavigatableFileExtra(moveFile)
                .putNotificationResourcesExtra(notificationResources)
    }
}