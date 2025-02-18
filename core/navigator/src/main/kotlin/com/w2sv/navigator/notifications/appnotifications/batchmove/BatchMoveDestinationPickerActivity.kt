package com.w2sv.navigator.notifications.appnotifications.batchmove

import android.net.Uri
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.log
import com.w2sv.common.util.takePersistableReadAndWriteUriPermission
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.moving.api.activity.AbstractDestinationPickerActivity
import com.w2sv.navigator.moving.batch.BatchMoveBroadcastReceiver
import com.w2sv.navigator.moving.model.DestinationSelectionManner
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.moving.model.NavigatorMoveDestination
import com.w2sv.navigator.notifications.NotificationResources
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import slimber.log.i

@AndroidEntryPoint
internal class BatchMoveDestinationPickerActivity : AbstractDestinationPickerActivity() {

    private val args: Args by threadUnsafeLazy {
        intent.getParcelableCompat<Args>(AbstractDestinationPickerActivity.Args.EXTRA)!!.log()
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
                ?: return sendMoveResultBundleAndFinishAndRemoveTask(MoveResult.InternalError)

        BatchMoveBroadcastReceiver.Companion.sendBroadcast(
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
    ) : AbstractDestinationPickerActivity.Args

    @Parcelize
    data class MoveFileWithNotificationResources(
        val moveFile: MoveFile,
        val notificationResources: NotificationResources
    ) : Parcelable
}
