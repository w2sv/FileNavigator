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
import com.w2sv.navigator.notifications.putMoveFileExtra
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

        // ===============
        // Intent Extras
        // ===============

        private val navigatableFile: NavigatableFile =
            savedStateHandle[NavigatableFile.EXTRA]!!

        val moveMediaFile: MediaFile? = navigatableFile.getSimpleStorageMediaFile(context)

        // ===============
        // DataStore Attributes
        // ===============

        fun saveLastManualMoveDestination(destination: Uri): Job =
            viewModelScope.launch {
                fileTypeRepository.saveLastManualMoveDestination(
                    navigatableFile.source,
                    destination
                )
            }

        fun getDestinationPickerStartLocation(): Uri? =
            fileTypeRepository.getLastManualMoveDestination(navigatableFile.source)
                ?: fileTypeRepository.getDefaultDestination(navigatableFile.source)
    }

    private val viewModel by viewModels<ViewModel>()

    private val destinationPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri ->
            i { "DocumentTree Uri: $treeUri" }

            // Exit on null treeUri (received on exiting folder picker via back press)
            treeUri ?: run {
                finish()
                return@registerForActivityResult
            }

            // Exit on unsuccessful conversion to SimpleStorage objects
            val targetDirectoryDocumentFile =
                DocumentFile.fromTreeUri(this, treeUri)

            if (targetDirectoryDocumentFile == null || viewModel.moveMediaFile == null) {
                finish()
                return@registerForActivityResult
            }

            contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            NewMoveFileNotificationManager.ResourcesCleanupBroadcastReceiver.startFromResourcesComprisingIntent(
                this,
                intent
            )

            // Move file
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.moveMediaFile!!.moveTo(
                    targetDirectoryDocumentFile,
                    callback = object : FileCallback() {
                        override fun onCompleted(result: Any) {
                            showToast(
                                getString(
                                    R.string.moved_file_to,
                                    targetDirectoryDocumentFile.getSimplePath(this@FileMoveActivity)
                                )
                            )
                        }

                        // TODO: refined errorCode handling
                        override fun onFailed(errorCode: ErrorCode) {
                            showToast(R.string.couldn_t_move_file)
                        }
                    }
                )
            }

            // Save targetDirectoryDocumentFile to DataStore and finish activity on saving completed
            viewModel
                .saveLastManualMoveDestination(targetDirectoryDocumentFile.uri)
                .invokeOnCompletion {
                    finish()
                }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        destinationPickerLauncher.launch(viewModel.getDestinationPickerStartLocation())
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
                .putMoveFileExtra(navigatableFile)
                .putNotificationResourcesExtra(notificationResources)
    }
}