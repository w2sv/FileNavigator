package com.w2sv.navigator.moving.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.takePersistableReadAndWriteUriPermission
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.coroutines.firstBlocking
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
    lateinit var globalScope: CoroutineScope

    private val moveBundle by lazy {
        MoveBundle.fromIntent<MoveBundle.QuickMove>(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        i { "onCreate" }

        destinationPicker.launch(moveBundle.destination.documentUri.uri)
        if (preferencesRepository.showQuickMovePermissionQueryExplanation.firstBlocking()) {
            startActivity(Intent(this, OverlayDialogActivity::class.java))
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
            globalScope.launch {
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
        fun start(
            moveBundle: MoveBundle.QuickMove,
            context: Context
        ) {
            context.startActivity(makeRestartActivityIntent(moveBundle, context))
        }

        fun makeRestartActivityIntent(
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