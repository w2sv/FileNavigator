package com.w2sv.navigator.moving.activity

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.uri.DocumentUri
import com.w2sv.common.util.takePersistableReadAndWriteUriPermission
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveFileNotificationData
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.MoveResult
import com.w2sv.navigator.moving.BatchMoveService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.parcelize.Parcelize
import slimber.log.i

@AndroidEntryPoint
internal class BatchMoveDestinationPickerActivity : DestinationPickerActivityApi() {

    @Inject
    lateinit var finisher: MoveActivityFinisher

    private val args: Args by threadUnsafeLazy {
        checkNotNull(intent.getParcelableCompat<Args>(DestinationPickerActivityApi.Args.EXTRA))
    }

    override fun launchPicker() {
        destinationPicker.launch(args.startDestination?.uri)
    }

    private val destinationPicker = registerForActivityResult(
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
            MoveDestination.Directory.fromTreeUri(this, treeUri)
                ?: return finisher.finishOnError(this, MoveResult.InternalError)

        startService(
            BatchMoveService.startIntent(
                context = applicationContext,
                args = BatchMoveService.Args(operations = args.buildOperations(moveDestination))
            )
        )
    }

    @Parcelize
    data class Args(val moveFilesWithNotificationResources: List<MoveFileNotificationData>, override val startDestination: DocumentUri?) :
        DestinationPickerActivityApi.Args {

        fun buildOperations(destination: MoveDestination.Directory): List<MoveOperation.DirectoryDestinationPicked> =
            moveFilesWithNotificationResources.map {
                MoveOperation.DirectoryDestinationPicked(
                    file = it.moveFile,
                    destinationSelectionManner = DestinationSelectionManner.Picked(it.cancelNotificationEvent),
                    destination = destination
                )
            }
    }
}
