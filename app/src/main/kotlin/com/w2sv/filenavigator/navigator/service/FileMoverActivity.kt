package com.w2sv.filenavigator.navigator.service

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
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.getSimplePath
import com.anggrayudi.storage.media.MediaFile
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.datastorage.datastore.preferences.AbstractPreferencesDataStoreRepository
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.datastore.PreferencesDataStoreRepository
import com.w2sv.filenavigator.navigator.MoveFile
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
class FileMoverActivity : ComponentActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        dataStoreRepository: PreferencesDataStoreRepository,
        @ApplicationContext context: Context
    ) :
        AbstractPreferencesDataStoreRepository.ViewModel<PreferencesDataStoreRepository>(
            dataStoreRepository
        ) {

        // ===============
        // Intent Extras
        // ===============

        val moveFile: MoveFile =
            savedStateHandle[FileNavigatorService.EXTRA_MOVE_FILE]!!

        val notificationParameters: MoveFile.NotificationParameters =
            savedStateHandle[MoveFile.NotificationParameters.EXTRA]!!

        // ===============
        // Extra Downstream
        // ===============

        val moveMediaFile: MediaFile? = moveFile.getMediaFile(context)

        // ===============
        // DataStore Attributes
        // ===============

        val defaultTargetDirDocumentUri: Uri? =
            dataStoreRepository.getUriFlow(moveFile.source.defaultDestination)
                .getValueSynchronously()
                .also {
                    i { "Retrieved ${moveFile.source.defaultDestination.preferencesKey} = $it" }
                }
    }

    private val viewModel by viewModels<ViewModel>()

    private val destinationSelectionLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { treeUri ->
            i { "DocumentTree Uri: $treeUri" }

            // Exit on null treeUri (received on exiting folder picker via back press)
            treeUri ?: run {
                finish()
                return@registerForActivityResult
            }

            // Exit on unsuccessful conversion to SimpleStorage objects
            val targetDirectoryDocumentFile =
                DocumentFile.fromTreeUri(this, treeUri) ?: run {
                    finish()
                    return@registerForActivityResult
                }
            viewModel.moveMediaFile ?: run {
                finish()
                return@registerForActivityResult
            }

            contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            viewModel.notificationParameters.cancelUnderlyingNotification(this)

            // Move file
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.moveMediaFile!!.moveTo(
                    targetDirectoryDocumentFile,
                    callback = object : FileCallback() {
                        override fun onCompleted(result: Any) {
                            showToast(
                                getString(
                                    R.string.moved_file_to,
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

            if (targetDirectoryDocumentFile != viewModel.defaultTargetDirDocumentUri && !viewModel.dataStoreRepository.getFileSourceDefaultDestinationIsLockedFlow(
                    viewModel.moveFile.source
                )
                    .getValueSynchronously()
            ) {
                // Save targetDirectoryDocumentFile to DataStore and finish activity on saving completed
                with(viewModel) {
                    saveToDataStore(
                        moveFile.source.defaultDestination.preferencesKey,
                        targetDirectoryDocumentFile.uri
                    )
                        .invokeOnCompletion {
                            i { "Saved ${targetDirectoryDocumentFile.uri} as ${moveFile.source.defaultDestination.preferencesKey} to preferences" }
                            finish()
                        }
                }
            } else {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        destinationSelectionLauncher.launch(viewModel.defaultTargetDirDocumentUri)
    }
}