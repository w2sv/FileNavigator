package com.w2sv.navigator.moving.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.takePersistableReadAndWriteUriPermission
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.navigator.MoveResultChannel
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.moving.receiver.MoveBroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@AndroidEntryPoint
internal class QuickMoveDestinationPermissionQueryActivity : AbstractMoveActivity() {

    @Inject
    override lateinit var moveResultChannel: MoveResultChannel

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var navigatorConfigDataSource: NavigatorConfigDataSource

    @Inject
    @GlobalScope(AppDispatcher.IO)
    lateinit var globalIoScope: CoroutineScope

    private val moveBundle by lazy {
        MoveBundle.fromIntent<MoveBundle.QuickMove>(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        i { "onCreate" }

        if (moveBundle.destination.hasReadAndWritePermission(this)) {
            i { "Destination has read & write permission; Starting MoveBroadcastReceiver" }
            MoveBroadcastReceiver.sendBroadcast(
                moveBundle = moveBundle,
                context = this
            )
            finishAndRemoveTask()
        } else {
            i { "Destination missing read & write permission; Launching destinationPicker" }
            destinationPicker.launch(moveBundle.destination.documentUri.uri)
            showExplanationDialogIfNotYetShown()
        }
    }

    private fun showExplanationDialogIfNotYetShown() {
        if (preferencesRepository.showQuickMovePermissionQueryExplanation.value) {
            i { "Starting QuickMoveDestinationPermissionQueryOverlayDialogActivity" }
            overlayDialogActivityLauncher.launch(
                Intent(
                    this,
                    QuickMoveDestinationPermissionQueryOverlayDialogActivity::class.java
                )
            )
        }
    }

    private val overlayDialogActivityLauncher: ActivityResultLauncher<Intent> by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                globalIoScope.launch {
                    preferencesRepository.showQuickMovePermissionQueryExplanation.save(
                        false
                    )
                }
            }
        }
    }

    private val destinationPicker =
        registerForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree(),
            callback = { treeUri ->
                if (treeUri != null) {
                    onAccessGranted(treeUri)
                }
                finishAndRemoveTask()
            }
        )

    private fun onAccessGranted(treeUri: Uri) {
        contentResolver.takePersistableReadAndWriteUriPermission(treeUri)

        // Build moveDestination, exit if unsuccessful
        val moveDestination =
            MoveDestination.Directory.fromTreeUri(this, treeUri)
                ?: return finishAndRemoveTask(MoveResult.InternalError)

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
        fun makeRestartActivityTaskIntent(
            moveBundle: MoveBundle.QuickMove,
            context: Context
        ): Intent =
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    QuickMoveDestinationPermissionQueryActivity::class.java
                )
            )
                .putExtra(MoveBundle.EXTRA, moveBundle)
    }
}