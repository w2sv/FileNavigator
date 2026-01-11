package com.w2sv.navigator.moving.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.content.restartActivityTaskIntent
import com.w2sv.common.logging.LoggingComponentActivity
import com.w2sv.common.util.takePersistableReadAndWriteUriPermission
import com.w2sv.composed.core.rememberStyledTextResource
import com.w2sv.core.common.R
import com.w2sv.designsystem.HighlightedDialogButton
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.navigator.domain.moving.MoveDestination
import com.w2sv.navigator.domain.moving.MoveOperation
import com.w2sv.navigator.domain.moving.MoveResult
import com.w2sv.navigator.moving.MoveBroadcastReceiver
import com.w2sv.navigator.moving.activity.QuickMoveDestinationAccessPermissionActivity.Action.LaunchPicker
import com.w2sv.navigator.moving.activity.QuickMoveDestinationAccessPermissionActivity.Action.ShowRationale
import com.w2sv.navigator.moving.activity.QuickMoveDestinationAccessPermissionActivity.Action.StartMove
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import slimber.log.i

/**
 * Entry activity for resolving destination access for a quick-move operation.
 *
 * This activity determines whether the quick-move destination already has persistable read/write permissions.
 * If so, the move is started immediately by forwarding the operation to the [MoveBroadcastReceiver].
 * Otherwise, the user is either shown a rationale explaining why access is required or prompted to select a destination via the system
 * document picker.
 *
 * Once access is granted, the permission is persisted and the move operation is dispatched, after which the activity finishes.
 */
@AndroidEntryPoint
internal class QuickMoveDestinationAccessPermissionActivity : LoggingComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var navigatorConfigDataSource: NavigatorConfigDataSource

    @Inject
    lateinit var finisher: MoveActivityFinisher

    private val moveOperation by threadUnsafeLazy { MoveOperation<MoveOperation.QuickMove>(intent) }

    private val destinationPicker = registerForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        callback = { treeUri ->
            if (treeUri != null) {
                onDocumentTreeAccessGranted(treeUri)
            }
            finishAndRemoveTask()
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            when (determineAction()) {
                StartMove -> {
                    i { "Destination has read & write permission; Starting MoveBroadcastReceiver" }
                    MoveBroadcastReceiver.sendBroadcast(
                        operation = moveOperation,
                        context = this@QuickMoveDestinationAccessPermissionActivity
                    )
                    finishAndRemoveTask()
                }

                ShowRationale -> {
                    i { "Showing rationale" }
                    setContent {
                        GrantRationalDialog(
                            onDismissRequest = {
                                lifecycleScope.launch {
                                    preferencesRepository
                                        .showQuickMovePermissionQueryExplanation
                                        .save(true)
                                }
                                launchDestinationPicker()
                            }
                        )
                    }
                }

                LaunchPicker -> {
                    i { "Destination missing read & write permission" }
                    launchDestinationPicker()
                }
            }
        }
    }

    private suspend fun determineAction(): Action =
        when {
            preferencesRepository.showQuickMovePermissionQueryExplanation.first() -> ShowRationale
            moveOperation.destination.hasReadAndWritePermission(this) -> StartMove
            else -> LaunchPicker
        }

    private fun launchDestinationPicker() {
        destinationPicker.launch(moveOperation.destination.documentUri.uri)
    }

    private fun onDocumentTreeAccessGranted(treeUri: Uri) {
        // Take persistable permissions so that the next time we dont need the user grant permission again for the destination
        contentResolver.takePersistableReadAndWriteUriPermission(treeUri)

        // Build moveDestination, exit if unsuccessful
        val moveDestination =
            MoveDestination.Directory.fromTreeUri(this, treeUri)
                ?: return finisher.finishOnError(this, MoveResult.InternalError)

        // If user selected different destination, save as quick move destination
        if (moveDestination != moveOperation.destination) {
            lifecycleScope.launch {
                navigatorConfigDataSource.saveQuickMoveDestination(
                    fileType = moveOperation.file.fileType,
                    sourceType = moveOperation.file.sourceType,
                    destination = moveDestination
                )
            }
        }

        MoveBroadcastReceiver.sendBroadcast(
            operation = moveOperation.copy(destination = moveDestination),
            context = this
        )
    }

    private enum class Action {
        StartMove,
        ShowRationale,
        LaunchPicker
    }

    companion object {
        fun intent(moveOperation: MoveOperation.QuickMove, context: Context): Intent =
            restartActivityTaskIntent<QuickMoveDestinationAccessPermissionActivity>(context)
                .putExtra(MoveOperation.EXTRA, moveOperation)
    }
}

@Composable
private fun GrantRationalDialog(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        modifier = modifier,
        icon = { Icon(Icons.Outlined.Info, null, modifier = Modifier.size(32.dp)) },
        text = {
            Column {
                Text(
                    rememberStyledTextResource(R.string.quick_move_permission_query_dialog_line_1),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Text(rememberStyledTextResource(R.string.quick_move_permission_query_dialog_line_2))
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = { HighlightedDialogButton(text = stringResource(R.string.understood), onClick = onDismissRequest) },
        properties = DialogProperties(dismissOnBackPress = false) // To assure user doesn't accidentally dismiss it without having read it
    )
}

@Preview
@Composable
private fun Prev() {
    AppTheme {
        GrantRationalDialog({})
    }
}
