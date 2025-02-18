package com.w2sv.navigator.notifications.appnotifications.movefile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.documentUri
import com.w2sv.common.util.log
import com.w2sv.common.util.takePersistableReadAndWriteUriPermission
import com.w2sv.kotlinutils.coroutines.flow.emit
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.api.activity.AbstractDestinationPickerActivity
import com.w2sv.navigator.moving.model.DestinationSelectionManner
import com.w2sv.navigator.moving.model.MediaIdWithMediaType
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveFile
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.moving.model.NavigatorMoveDestination
import com.w2sv.navigator.notifications.NotificationResources
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.parcelize.Parcelize
import slimber.log.i

@AndroidEntryPoint
internal class FileDestinationPickerActivity : AbstractDestinationPickerActivity() {

    @Inject
    lateinit var blacklistedMediaUris: MutableSharedFlow<MediaIdWithMediaType>

    private val args: Args by threadUnsafeLazy {
        intent.getParcelableCompat<Args>(AbstractDestinationPickerActivity.Args.EXTRA)!!
    }

    override fun preemptiveMoveFailure(): MoveResult.Failure? =
        when {
            !args.moveFile.mediaStoreFileData.fileExists -> MoveResult.MoveFileNotFound
            else -> super.preemptiveMoveFailure()
        }

    override fun launchPicker() {
        documentCreator.launch(args.moveFile.mediaStoreFileData.name)
    }

    /**
     * Must be lazy, as it accesses [args], which depend on the activity's [getIntent] being non-null, which is only guaranteed [onCreate].
     * Will be initialized during [onCreate], which calls [launchPicker].
     */
    private val documentCreator by threadUnsafeLazy {
        registerForActivityResult(
            object :
                ActivityResultContracts.CreateDocument(args.moveFile.fileType.simpleStorageMediaType.mimeType) {
                override fun createIntent(context: Context, input: String): Intent =
                    super.createIntent(context, input)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .apply {
                            // Set picker start location
                            intent.putExtra(
                                DocumentsContract.EXTRA_INITIAL_URI,
                                args.pickerStartDestination?.uri
                            )
                        }
            },
            callback = { contentUri ->
                if (contentUri != null) {
                    onDocumentCreated(contentUri)
                }
                finishAndRemoveTask()
            }
        )
    }

    /**
     * @param contentUri E.g.: __content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots%2FScreenshot_2024-09-08-01-29-28-913_com.w2sv.filenavigator.debug.jpg__
     *
     * Google Drive: __content://com.google.android.apps.docs.storage/document/acc%3D5%3Bdoc%3Dencoded%3D-y1_RtvvQJ6uU4hFl-Z6Qo5ggFvP7XIezps-y0zmy_C3AUxHjAy8XzxYIuxm7wpaISVd__
     */
    private fun onDocumentCreated(contentUri: Uri) {
        i { "Document contentUri: $contentUri" }

        contentResolver.takePersistableReadAndWriteUriPermission(contentUri) // TODO: necessary?

        val destination = NavigatorMoveDestination.File.get(contentUri.documentUri, this)
            .also {
                // In case of local file, emit MediaIdWithMediaType on blacklistedMediaUris to
                // prevent notification emission for created move destination file
                it.localOrNull?.let { localFileDestination ->
                    blacklistedMediaUris.emit(
                        value = MediaIdWithMediaType(
                            mediaId = localFileDestination.mediaUri.id!!,
                            mediaType = args.moveFile.fileType.simpleStorageMediaType
                        )
                            .log { "Emitting $it" },
                        scope = lifecycleScope
                    )
                }
            }

        MoveBroadcastReceiver.sendBroadcast(
            moveBundle = MoveBundle.FileDestinationPicked(
                file = args.moveFile,
                destination = destination,
                destinationSelectionManner = DestinationSelectionManner.Picked(args.notificationResources)
            ),
            context = this
        )
    }

    @Parcelize
    data class Args(
        val moveFile: MoveFile,
        val notificationResources: NotificationResources,
        override val pickerStartDestination: DocumentUri?
    ) : AbstractDestinationPickerActivity.Args
}
