package com.w2sv.navigator.notifications.managers.newmovefile.actionexecutors

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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.getSimplePath
import com.anggrayudi.storage.media.MediaFile
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.data.storage.repositories.FileTypeRepository
import com.w2sv.navigator.R
import com.w2sv.navigator.model.NavigatableFile
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.newmovefile.NewMoveFileNotificationManager
import com.w2sv.navigator.notifications.putNavigatableFileExtra
import com.w2sv.navigator.notifications.putNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class FileMoveActivity : ComponentActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        private val fileTypeRepository: FileTypeRepository,
        @ApplicationContext context: Context
    ) :
        androidx.lifecycle.ViewModel() {

        private val navigatableFile: NavigatableFile =
            savedStateHandle[NavigatableFile.EXTRA]!!

        val moveFileExists: Boolean
            get() = navigatableFile.mediaStoreFile.columnData.fileExists

        val moveMediaFile: MediaFile? = navigatableFile.getSimpleStorageMediaFile(context)

        // ===============
        // FileTypeRepository
        // ===============

        fun saveLastMoveDestination(destination: Uri): Job =
            viewModelScope.launch {
                fileTypeRepository.saveLastMoveDestination(
                    navigatableFile.source,
                    destination
                )
            }

        val lastMoveDestination = fileTypeRepository.getLastMoveDestination(navigatableFile.source)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            viewModel.moveMediaFile == null -> return showResultToastAndFinishActivity(getString(R.string.internal_error))
            !viewModel.moveFileExists -> {
                showResultToastAndFinishActivity(getString(R.string.couldn_t_move_file_has_already_been_moved_deleted_or_renamed))
                removeNotificationAndCleanupResources()
                return
            }
        }

        destinationPickerLauncher.launch(viewModel.lastMoveDestination)
    }

    private val viewModel by viewModels<ViewModel>()

    private val destinationPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree(), ::onTargetDirSelected)

    private fun onTargetDirSelected(treeUri: Uri?) {
        i { "Move destination DocumentTree Uri: $treeUri" }

        // Exit on null treeUri (received on exiting folder picker via back press)
        treeUri ?: return showResultToastAndFinishActivity()

        // Take persistable read & write permission
        // Required for quick move by remedying of "Failed query: java.lang.SecurityException: Permission Denial: opening provider com.android.externalstorage.ExternalStorageProvider from ProcessRecord{6fc17ee 8097:com.w2sv.filenavigator.debug/u0a753} (pid=8097, uid=10753) requires that you obtain access using ACTION_OPEN_DOCUMENT or related APIs"
        contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        // Exit on unsuccessful conversion to DocumentFile
        val targetDirectoryDocumentFile =
            DocumentFile.fromTreeUri(this, treeUri)
                ?: return showResultToastAndFinishActivity(getString(R.string.internal_error))

        // Move file
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.moveMediaFile!!.moveTo(
                targetFolder = targetDirectoryDocumentFile,
                callback = object : FileCallback() {
                    override fun onCompleted(result: Any) {
                        removeNotificationAndCleanupResources()

                        viewModel
                            .saveLastMoveDestination(targetDirectoryDocumentFile.uri)
//                                .invokeOnCompletion {
//                                    finishAndRemoveTask()
//                                }

                        showResultToastAndFinishActivity(
                            message = getString(
                                R.string.moved_file_to,
                                targetDirectoryDocumentFile.getSimplePath(applicationContext)
                            )
                        )
                    }

                    override fun onFailed(errorCode: ErrorCode) {
                        i { errorCode.toString() }
                        showResultToastAndFinishActivity(errorCode.name)
                    }
                }
            )
        }
    }

    private fun removeNotificationAndCleanupResources() {
        NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
            applicationContext,
            intent
        )
    }

    private fun showResultToastAndFinishActivity(message: String? = null) {
        message?.let(::showToast)
        finishAndRemoveTask()
    }

    companion object {
        fun makeRestartActivityIntent(
            navigatableFile: NavigatableFile,
            notificationResources: NotificationResources,
            context: Context
        ): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    FileMoveActivity::class.java
                )
            )
                .putNavigatableFileExtra(navigatableFile)
                .putNotificationResourcesExtra(notificationResources)
    }
}