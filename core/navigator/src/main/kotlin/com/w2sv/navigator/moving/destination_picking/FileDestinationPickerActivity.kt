package com.w2sv.navigator.moving.destination_picking

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.log
import com.w2sv.common.utils.takePersistableReadAndWriteUriPermission
import com.w2sv.domain.model.MoveDestination
import com.w2sv.navigator.MoveResultChannel
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
internal class FileDestinationPickerActivity : DestinationPickerActivity() {

    @Inject
    override lateinit var moveResultChannel: MoveResultChannel

    private val args: Args by lazy {
        intent.getParcelableCompat<Args>(DestinationPickerActivity.Args.EXTRA)!!
    }

    override fun preemptiveMoveFailure(): MoveResult.Failure? =
        when {
            !args.moveFile.mediaStoreFileData.fileExists -> MoveResult.MoveFileNotFound
            else -> super.preemptiveMoveFailure()
        }

    override fun launchPicker() {
        documentCreator.launch(args.moveFile.mediaStoreFileData.name)
    }

    private val documentCreator by lazy {
        registerForActivityResult(
            contract = ActivityResultContracts.CreateDocument(args.moveFile.fileType.simpleStorageMediaType.mimeType),  // TODO: set mime types for non-simpleStorageMediaTypes
            callback = { contentUri ->
                if (contentUri == null) {  // When exited via back press
                    finishAndRemoveTask()
                } else {
                    onDocumentCreated(contentUri)
                }
            }
        )
    }

    /**
     * @param contentUri E.g. content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots%2FScreenshot_2024-09-08-01-29-28-913_com.w2sv.filenavigator.debug.jpg
     */
    private fun onDocumentCreated(contentUri: Uri) {
        i { "Document contentUri: $contentUri" }

        // Take persistable read & write permission
        // Required for quick move
        contentResolver.takePersistableReadAndWriteUriPermission(contentUri)  // TODO: necessary?

        val destination =
            DocumentFile.fromSingleUri(this, contentUri)!!.log { "Converted documentFile: $it" }

        MoveBroadcastReceiver.sendBroadcast(
            moveBundle = MoveBundle(
                file = args.moveFile,
                destination = MoveDestination.fromDocumentFile(destination),
                mode = MoveMode.DestinationPicked(args.notificationResources)
            ),
            context = this
        )

        finishAndRemoveTask()
    }

    @Parcelize
    data class Args(
        val moveFile: MoveFile,
        val notificationResources: NotificationResources,
        override val pickerStartDestination: DocumentUri?,
    ) : DestinationPickerActivity.Args
}