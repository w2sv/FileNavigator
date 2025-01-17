package com.w2sv.navigator.moving.activity.destination_picking

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.takePersistableReadAndWriteUriPermission
import com.w2sv.navigator.MoveResultChannel
import com.w2sv.navigator.moving.model.DestinationSelectionManner
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFileWithNotificationResources
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.moving.model.NavigatorMoveDestination
import com.w2sv.navigator.moving.receiver.BatchMoveBroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.parcelize.Parcelize
import slimber.log.i

@AndroidEntryPoint
internal class FileBatchDestinationPickerActivity : DestinationPickerActivity() {

    @Inject
    override lateinit var moveResultChannel: MoveResultChannel

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
                if (treeUri != null) {
                    onMoveDestinationSelected(treeUri)
                }
                finishAndRemoveTask()
            }
        )

    private fun onMoveDestinationSelected(treeUri: Uri) {
        i { "Move destination treeUri: $treeUri" }

        // Take persistable read & write permission
        // Required for quick move
        contentResolver.takePersistableReadAndWriteUriPermission(treeUri)

        // Build moveDestination, exit if unsuccessful
        val moveDestination =
            NavigatorMoveDestination.Directory.fromTreeUri(this, treeUri)
                ?: return finishAndRemoveTask(MoveResult.InternalError)

        i { args.toString() }

        BatchMoveBroadcastReceiver.sendBroadcast(
            args = BatchMoveBroadcastReceiver.Args(
                batchMoveBundles = args.moveFilesWithNotificationResources.map {
                    MoveBundle.DirectoryDestinationPicked(
                        file = it.moveFile,
                        destinationSelectionManner = DestinationSelectionManner.Picked(
                            notificationResources = it.notificationResources
                        ),
                        destination = moveDestination
                    )
                }
            ),
            context = applicationContext
        )
    }

    @Parcelize
    data class Args(
        val moveFilesWithNotificationResources: List<MoveFileWithNotificationResources>,
        override val pickerStartDestination: DocumentUri?
    ) : DestinationPickerActivity.Args
}
