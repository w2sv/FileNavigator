package com.w2sv.filenavigator.service

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.getSimplePath
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.androidutils.notifying.getNotificationManager
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.mediastore.MediaStoreFile
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class FileMoverActivity : ComponentActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle
    ) :
        androidx.lifecycle.ViewModel() {

        val mediaStoreFile: MediaStoreFile =
            savedStateHandle[FileListenerService.EXTRA_MEDIA_STORE_FILE]!!
        val cancelNotificationId: Int =
            savedStateHandle[FileListenerService.EXTRA_NOTIFICATION_ID]!!
        val requestCodes: ArrayList<Int> =
            savedStateHandle[FileListenerService.EXTRA_REQUEST_CODES]!!
    }

    private val viewModel by viewModels<ViewModel>()

    private val destinationSelectionLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri ->
            i { "DocumentTree Uri: $treeUri" }

            treeUri ?: return@registerForActivityResult

            val targetDirectoryDocumentFile =
                DocumentFile.fromTreeUri(this, treeUri) ?: return@registerForActivityResult
            val mediaFile = MediaStoreCompat.fromMediaId(
                this,
                viewModel.mediaStoreFile.type.storageType,
                viewModel.mediaStoreFile.data.id
            ) ?: return@registerForActivityResult

            contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            lifecycleScope.launch(Dispatchers.IO) {
                mediaFile.moveTo(
                    targetDirectoryDocumentFile,
                    callback = object : FileCallback() {
                        override fun onCompleted(result: Any) {
                            showToast(
                                getString(
                                    R.string.successfully_moved_file_to,
                                    targetDirectoryDocumentFile.getSimplePath(this@FileMoverActivity)
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

            finishAndRemoveTask()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getNotificationManager().cancel(viewModel.cancelNotificationId)

        FileListenerService.cleanUpIds(
            viewModel.cancelNotificationId,
            viewModel.requestCodes,
            this
        )

        i { "Launching destinationSelectionLauncher" }
        destinationSelectionLauncher.launch(viewModel.mediaStoreFile.uri)
    }
}