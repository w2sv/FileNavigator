package com.w2sv.navigator.moving.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.content.getParcelableCompat
import com.w2sv.common.logging.log
import com.w2sv.common.uri.DocumentUri
import com.w2sv.common.uri.documentUri
import com.w2sv.common.util.takePersistableReadAndWriteUriPermission
import com.w2sv.kotlinutils.coroutines.flow.emit
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.navigator.domain.moving.MediaIdWithMediaType
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveFile
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.notifications.CancelNotificationEvent
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.parcelize.Parcelize
import slimber.log.i

@AndroidEntryPoint
internal class FileDestinationPickerActivity : DestinationPickerActivityApi() {

    @Inject
    lateinit var blacklistedMediaUris: MutableSharedFlow<MediaIdWithMediaType>

    private val args: Args by threadUnsafeLazy {
        checkNotNull(intent.getParcelableCompat<Args>(DestinationPickerActivityApi.Args.EXTRA))
    }

    override fun launchPicker() {
        documentCreator.launch(args.moveFile.mediaStoreData.name)
    }

    /**
     * Must be lazy, as it accesses [args], which depend on the activity's [getIntent] being non-null, which is only guaranteed [onCreate].
     * Will be initialized during [onCreate], which calls [launchPicker].
     */
    private val documentCreator by threadUnsafeLazy {
        registerForActivityResult(
            object :
                ActivityResultContracts.CreateDocument(args.moveFile.fileType.mediaType.mimeType) {
                override fun createIntent(context: Context, input: String): Intent =
                    super.createIntent(context, input)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .apply {
                            // Set picker start location
                            intent.putExtra(
                                DocumentsContract.EXTRA_INITIAL_URI,
                                args.startDestination?.uri
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

        val destination = MoveDestination.File(contentUri.documentUri, this)
            .also {
                // In case of local file, emit MediaIdWithMediaType on blacklistedMediaUris to
                // prevent notification emission for created move destination file
                it.localOrNull?.let { localFileDestination ->
                    blacklistedMediaUris.emit(
                        value = MediaIdWithMediaType(
                            mediaId = localFileDestination.mediaUri.id()!!,
                            mediaType = args.moveFile.fileType.mediaType
                        )
                            .log { "Emitting $it" },
                        scope = lifecycleScope
                    )
                }
            }

        MoveBroadcastReceiver.sendBroadcast(
            operation = args.moveOperation(destination),
            context = this
        )
    }

    @Parcelize
    data class Args(val moveFile: MoveFile, val cancelNotification: CancelNotificationEvent, override val startDestination: DocumentUri?) :
        DestinationPickerActivityApi.Args {
        fun moveOperation(destination: MoveDestination.File): MoveOperation.FileDestinationPicked =
            MoveOperation.FileDestinationPicked(
                file = moveFile,
                destination = destination,
                destinationSelectionManner = DestinationSelectionManner.Picked(cancelNotification)
            )
    }
}
