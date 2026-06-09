package com.w2sv.navigator.moving.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.content.getParcelableCompat
import com.w2sv.core.logging.log
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.domain.moving.DestinationSelectionManner
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.NavigatableFile
import com.w2sv.navigator.domain.notifications.CancelNotificationEvent
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.shared.createdfiles.EmitSelfCreatedFile
import com.w2sv.navigator.shared.createdfiles.SelfCreatedFileIdentifiers
import com.w2sv.storage.uri.DocumentUri
import com.w2sv.storage.uri.documentUri
import com.w2sv.storage.util.takePersistableReadAndWriteUriPermission
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import slimber.log.i

@AndroidEntryPoint
internal class FileDestinationPickerActivity : DestinationPickerActivityApi() {

    @Inject
    lateinit var publishSelfCreatedFile: EmitSelfCreatedFile

    private val args: Args by threadUnsafeLazy {
        checkNotNull(intent.getParcelableCompat<Args>(DestinationPickerActivityApi.Args.EXTRA))
    }

    override fun launchPicker() {
        documentCreator.launch(args.navigatableFile.mediaStoreEntry.fileName)
    }

    /**
     * Must be lazy, as it accesses [args], which depend on the activity's [getIntent] being non-null, which is only guaranteed [onCreate].
     * Will be initialized during [onCreate], which calls [launchPicker].
     */
    private val documentCreator by threadUnsafeLazy {
        registerForActivityResult(
            object :
                ActivityResultContracts.CreateDocument(args.navigatableFile.fileType.mediaType.mimeType) {
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
        // In case of local file, emit MediaIdWithMediaType on blacklistedMediaUris to
        // prevent notification emission for created move destination file
        destination.localOrNull?.let { blacklistCreatedFileDestination(it) }

        MoveBroadcastReceiver.sendBroadcast(
            operation = args.moveOperation(destination),
            context = this
        )
    }

    private fun blacklistCreatedFileDestination(destination: MoveDestination.File.Local) {
        lifecycleScope.launch {
            publishSelfCreatedFile(
                SelfCreatedFileIdentifiers(
                    mediaId = checkNotNull(destination.mediaUri.id()),
                    mediaType = args.navigatableFile.fileType.mediaType
                )
                    .log { "Publishing $it as SelfCreatedMediaId" }
            )
        }
    }

    @Parcelize
    data class Args(
        val navigatableFile: NavigatableFile,
        val cancelNotification: CancelNotificationEvent,
        override val startDestination: DocumentUri?
    ) : DestinationPickerActivityApi.Args {
        fun moveOperation(destination: MoveDestination.File): MoveOperation.FileDestinationPicked =
            MoveOperation.FileDestinationPicked(
                file = navigatableFile,
                destination = destination,
                destinationSelectionManner = DestinationSelectionManner.Picked(cancelNotification)
            )
    }
}
