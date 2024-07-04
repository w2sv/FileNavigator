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
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.common.utils.takePersistableReadAndWriteUriPermission
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.kotlinutils.coroutines.firstBlocking
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.shared.putMoveFileExtra
import com.w2sv.navigator.shared.putOptionalNotificationResourcesExtra
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
internal class MoveDestinationSelectionActivity : ComponentActivity() {

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        navigatorConfigDataSource: NavigatorConfigDataSource
    ) :
        androidx.lifecycle.ViewModel() {

        val moveFile: MoveFile =
            savedStateHandle[MoveFile.EXTRA]!!

        fun preemptiveMoveFailure(): MoveResult.Failure? =
            when {
                !isExternalStorageManger -> MoveResult.Failure.ManageAllFilesPermissionMissing
                !moveFile.mediaStoreData.fileExists -> MoveResult.Failure.MoveFileNotFound
                else -> null
            }

        val lastMoveDestination by lazy {
            navigatorConfigDataSource.lastMoveDestination(moveFile.fileType, moveFile.sourceType)
                .firstBlocking()
                .firstOrNull()
        }
    }

    @Inject
    lateinit var moveResultListener: MoveResultListener

    private val viewModel by viewModels<ViewModel>()

    private val notificationResources: NotificationResources?
        get() = NotificationResources.fromIntent(intent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (val moveFailure = viewModel.preemptiveMoveFailure()) {
            null -> destinationPickerLauncher.launch(viewModel.lastMoveDestination?.uri)
            else -> {
                finishAndRemoveTask(moveFailure)
            }
        }
    }

    private val destinationPickerLauncher =
        registerForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
            callback = { treeUri ->
                if (treeUri == null) {  // When file picker exited via back press
                    finishAndRemoveTask()
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
                ?: return finishAndRemoveTask(MoveResult.Failure.InternalError)

        MoveBroadcastReceiver.sendBroadcast(
            context = applicationContext,
            moveBundle = MoveBundle(
                file = viewModel.moveFile,
                destination = moveDestinationDocumentUri,
                mode = MoveMode.ManualSelection
            ),
            notificationResources = notificationResources
        )
        finishAndRemoveTask()
    }

    private fun finishAndRemoveTask(moveResult: MoveResult? = null) {
        moveResult?.let {
            moveResultListener.invoke(it, notificationResources)
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
                    MoveDestinationSelectionActivity::class.java
                )
            )
                .putMoveFileExtra(moveFile)
                .putOptionalNotificationResourcesExtra(notificationResources)
    }
}