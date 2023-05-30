package com.w2sv.filenavigator.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.media.MediaStoreCompat
import com.w2sv.androidutils.notifying.getNotificationManager
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.filenavigator.mediastore.MediaStoreFileMetadata
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme
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

        val mediaStoreFileMetadata: MediaStoreFileMetadata =
            savedStateHandle[FileNavigator.EXTRA_MEDIA_STORE_FILE_METADATA]!!
        val cancelNotificationId: Int = savedStateHandle[FileNavigator.EXTRA_NOTIFICATION_ID]!!
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
                viewModel.mediaStoreFileMetadata.mediaType.simpleStorageMediaType,
                viewModel.mediaStoreFileMetadata.mediaId
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
                            showToast("Successfully moved file")
                        }

                        // TODO: refined errorCode handling
                        override fun onFailed(errorCode: ErrorCode) {
                            showToast("Couldn't move file")
                        }
                    }
                )
            }

            finishAndRemoveTask()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FileNavigatorTheme {
                FileNavigatorTheme {
                    Surface {}
                }
            }
        }

        getNotificationManager().cancel(viewModel.cancelNotificationId)

        i { "Launching destinationSelectionLauncher" }
        destinationSelectionLauncher.launch(viewModel.mediaStoreFileMetadata.mediaUri)
    }

    companion object {
        fun getPendingIntent(
            context: Context,
            mediaStoreFileMetadata: MediaStoreFileMetadata,
            cancelNotificationId: Int
        ): PendingIntent =
            PendingIntent.getActivity(
                context,
                PendingIntentRequestCode.MoveFile.ordinal,
                getIntent(context, mediaStoreFileMetadata, cancelNotificationId),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
            )

        private fun getIntent(
            context: Context,
            mediaStoreFileMetadata: MediaStoreFileMetadata,
            cancelNotificationId: Int
        ): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    FileMoverActivity::class.java
                )
            )
                .putExtra(FileNavigator.EXTRA_MEDIA_STORE_FILE_METADATA, mediaStoreFileMetadata)
                .putExtra(
                    FileNavigator.EXTRA_NOTIFICATION_ID,
                    cancelNotificationId
                )
    }
}