package com.w2sv.navigator.moving

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.common.utils.takePersistableReadAndWriteUriPermission
import com.w2sv.domain.model.MoveDestination
import com.w2sv.navigator.moving.model.BatchMoveBundle
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveFileWithNotificationResources
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
internal class DestinationPickerActivity : ComponentActivity() {

    private val args: Args by lazy {
        intent.getParcelableCompat<Args>(Args.EXTRA)!!
    }

    private fun preemptiveMoveFailure(): MoveResult.Failure? =
        when {
            !isExternalStorageManger -> MoveResult.Failure.ManageAllFilesPermissionMissing
            (args as? Args.SingleFile)?.moveFile?.mediaStoreFileData?.fileExists == false -> MoveResult.Failure.MoveFileNotFound
            else -> null
        }

    @Inject
    lateinit var moveResultListener: MoveResultListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (val moveFailure = preemptiveMoveFailure()) {
            null -> destinationPickerLauncher.launch(args.pickerStartDestination?.uri)
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

        // Build moveDestination, exit if unsuccessful
        val moveDestination =
            MoveDestination.fromTreeUri(this, treeUri)
                ?: return finishAndRemoveTask(MoveResult.Failure.InternalError)

        i { args.toString() }

        when (val capturedArgs = args) {
            is Args.SingleFile -> MoveBroadcastReceiver.sendBroadcast(
                moveBundle = MoveBundle(
                    file = capturedArgs.moveFile,
                    destination = moveDestination,
                    mode = MoveMode.DestinationPicked(capturedArgs.notificationResources)
                ),
                applicationContext
            )

            is Args.FileBatch -> BatchMoveBroadcastReceiver.sendBroadcast(
                BatchMoveBroadcastReceiver.Args(
                    batchMoveBundles = capturedArgs.moveFilesWithNotificationResources.map {
                        BatchMoveBundle(
                            moveFile = it.moveFile,
                            moveMode = MoveMode.DestinationPicked(
                                notificationResources = it.notificationResources,
                                isPartOfBatch = true
                            )
                        )
                    },
                    destination = moveDestination,
                ),
                applicationContext
            )
        }
        finishAndRemoveTask()
    }

    private fun finishAndRemoveTask(moveFailure: MoveResult.Failure? = null) {
        moveFailure?.let {
            moveResultListener.onPreMoveCancellation(
                moveFailure = it,
                notificationResources = (args as? Args.SingleFile)?.notificationResources
            )
        }
        finishAndRemoveTask()
    }

    @Parcelize
    sealed interface Args : Parcelable {
        val pickerStartDestination: DocumentUri?

        @Parcelize
        data class SingleFile(
            val moveFile: MoveFile,
            val notificationResources: NotificationResources,
            override val pickerStartDestination: DocumentUri?,
        ) : Args

        @Parcelize
        data class FileBatch(
            val moveFilesWithNotificationResources: List<MoveFileWithNotificationResources>,
            override val pickerStartDestination: DocumentUri?,
        ) : Args

        companion object {
            const val EXTRA = "com.w2sv.navigator.extra.MoveDestinationSelectionActivity.Args"
        }
    }

    companion object {
        fun makeRestartActivityIntent(
            args: Args,
            context: Context
        ): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    DestinationPickerActivity::class.java
                )
            )
                .putExtra(Args.EXTRA, args)
    }
}