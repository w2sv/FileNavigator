package com.w2sv.navigator.moving

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.takePersistableReadAndWriteUriPermission
import com.w2sv.domain.model.MoveDestination
import com.w2sv.navigator.moving.model.BatchMoveBundle
import com.w2sv.navigator.moving.model.MoveFileWithNotificationResources
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.moving.model.MoveResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
internal class FileBatchDestinationPickerActivity : DestinationPickerActivity() {

    @Inject
    override lateinit var moveResultListener: MoveResultListener

    private val args: Args by lazy {
        intent.getParcelableCompat<Args>(DestinationPickerActivity.Args.EXTRA)!!
    }

    override fun launchPicker() {
        destinationPicker.launch(args.pickerStartDestination?.uri)
    }

    private val destinationPicker =
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

        BatchMoveBroadcastReceiver.sendBroadcast(
            BatchMoveBroadcastReceiver.Args(
                batchMoveBundles = args.moveFilesWithNotificationResources.map {
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

        finishAndRemoveTask()
    }

    @Parcelize
    data class Args(
        val moveFilesWithNotificationResources: List<MoveFileWithNotificationResources>,
        override val pickerStartDestination: DocumentUri?,
    ) : DestinationPickerActivity.Args
}