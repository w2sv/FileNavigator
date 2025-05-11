package com.w2sv.navigator.moving.quick

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.util.takePersistableReadAndWriteUriPermission
import com.w2sv.core.common.R
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.moving.model.MoveDestination
import com.w2sv.navigator.shared.DialogHostingActivity
import com.w2sv.navigator.shared.roundedCornersAlertDialogBuilder
import com.w2sv.navigator.shared.setIconHeader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i

@AndroidEntryPoint
internal class QuickMoveDestinationAccessPermissionQueryActivity : DialogHostingActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var navigatorConfigDataSource: NavigatorConfigDataSource

    @Inject
    @GlobalScope(AppDispatcher.IO)
    lateinit var globalIoScope: CoroutineScope

    private val moveBundle by threadUnsafeLazy {
        MoveBundle.fromIntent<MoveBundle.QuickMove>(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (moveBundle.destination.hasReadAndWritePermission(this)) {
            i { "Destination has read & write permission; Starting MoveBroadcastReceiver" }
            MoveBroadcastReceiver.sendBroadcast(
                moveBundle = moveBundle,
                context = this
            )
            finishAndRemoveTask()
        } else {
            i { "Destination missing read & write permission" }

            if (preferencesRepository.showQuickMovePermissionQueryExplanation.value) {
                i { "Showing rational" }
                showDestinationPickerRational {
                    globalIoScope.launch { preferencesRepository.showQuickMovePermissionQueryExplanation.save(true) }
                    launchQuickMoveDestinationPicker()
                }
            } else {
                launchQuickMoveDestinationPicker()
            }
        }
    }

    private fun showDestinationPickerRational(onPositiveButtonClick: () -> Unit) {
        showDialog(
            roundedCornersAlertDialogBuilder(this)
                .setIconHeader(R.drawable.ic_info_outline_24)
                .setMessage(getString(R.string.quick_move_permission_query_dialog_content))
                .setPositiveButton(getString(R.string.understood)) { _, _ -> onPositiveButtonClick() }
                .setCancelable(false)
        )
    }

    private fun launchQuickMoveDestinationPicker() {
        destinationPicker.launch(moveBundle.destination.documentUri.uri)
    }

    private val destinationPicker = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        callback = { treeUri ->
            if (treeUri != null) {
                onDocumentTreeAccessGranted(treeUri)
            }
            finishAndRemoveTask()
        }
    )

    private fun onDocumentTreeAccessGranted(treeUri: Uri) {
        contentResolver.takePersistableReadAndWriteUriPermission(treeUri)

        // Build moveDestination, exit if unsuccessful
        val moveDestination =
            MoveDestination.Directory.fromTreeUri(this, treeUri)
                ?: return sendMoveResultBundleAndFinishAndRemoveTask(MoveResult.InternalError)

        // If user selected different destination, save as quick move destination
        if (moveDestination != moveBundle.destination) {
            globalIoScope.launch {
                navigatorConfigDataSource.saveQuickMoveDestination(
                    fileType = moveBundle.file.fileType,
                    sourceType = moveBundle.file.sourceType,
                    destination = moveDestination
                )
            }
        }

        MoveBroadcastReceiver.sendBroadcast(
            moveBundle = moveBundle.copy(destination = moveDestination),
            context = this
        )
    }

    companion object {
        fun makeRestartActivityTaskIntent(moveBundle: MoveBundle.QuickMove, context: Context): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    QuickMoveDestinationAccessPermissionQueryActivity::class.java
                )
            )
                .putExtra(MoveBundle.EXTRA, moveBundle)
    }
}
