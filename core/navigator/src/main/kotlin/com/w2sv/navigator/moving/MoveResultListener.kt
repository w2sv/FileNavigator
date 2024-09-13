package com.w2sv.navigator.moving

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.androidutils.res.getText
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.core.navigator.R
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.usecase.InsertMoveEntryUseCase
import com.w2sv.kotlinutils.coroutines.collectFromFlow
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.MoveResultChannel
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.AutoMoveDestinationInvalidNotificationManager
import com.w2sv.navigator.notifications.managers.MoveFileNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import slimber.log.i
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@ServiceScoped
internal class MoveResultListener @Inject constructor(
    moveResultChannel: MoveResultChannel,
    private val insertMoveEntryUseCase: InsertMoveEntryUseCase,
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val moveFileNotificationManager: MoveFileNotificationManager,
    private val autoMoveDestinationInvalidNotificationManager: AutoMoveDestinationInvalidNotificationManager,
    @GlobalScope(AppDispatcher.Default) private val scope: CoroutineScope,  // TODO
    @ApplicationContext private val context: Context
) {
    init {
        scope.collectFromFlow(moveResultChannel.receiveAsFlow()) { moveResultBundle ->
            i { "Received $moveResultBundle" }

            when (moveResultBundle) {
                is MoveResult.Bundle.PreCheckFailure -> {
                    onPreCheckFailure(
                        moveFailure = moveResultBundle.moveFailure,
                        notificationResources = moveResultBundle.notificationResources
                    )
                }

                is MoveResult.Bundle.PostMoveBundleCreation -> {
                    onResult(
                        moveResult = moveResultBundle.moveResult,
                        moveBundle = moveResultBundle.moveBundle
                    )
                }
            }
        }
    }

    private suspend fun onPreCheckFailure(
        moveFailure: MoveResult.Failure,
        notificationResources: NotificationResources?
    ) {
        if (moveFailure.cancelNotification && notificationResources != null) {
            cancelNotification(notificationResources)
        }

        moveFailure.explanationStringRes?.let {
            context.showMoveFailureToast(it)
        }
    }

    private suspend fun onResult(
        moveResult: MoveResult,
        moveBundle: MoveBundle
    ) {
        if (moveResult.cancelNotification && moveBundle.mode is MoveMode.NotificationBased) {
            cancelNotification(moveBundle.mode.notificationResources)
        }
        when (moveResult) {
            is MoveResult.Success -> {
                onSuccess(moveBundle)
            }

            is MoveResult.Failure -> {
                if (moveResult.explanationStringRes != null && moveBundle.mode.showMoveResultToast) {
                    context.showMoveFailureToast(moveResult.explanationStringRes)
                }
                if (moveResult == MoveResult.MoveDestinationNotFound) {
                    when (moveBundle.mode) {
                        is MoveMode.Auto -> {
                            onAutoMoveDestinationNotFound(moveBundle)
                        }

                        is MoveMode.Quick -> {
                            onQuickMoveDestinationNotFound(
                                moveBundle = moveBundle
                            )
                        }

                        is MoveMode.DestinationPicked -> {  // Shouldn't normally occur
                            onResult(
                                moveBundle = moveBundle,
                                moveResult = MoveResult.InternalError
                            )
                        }
                    }
                }
            }
        }
    }

    private fun cancelNotification(notificationResources: NotificationResources?) {
        notificationResources?.cancelNotification(context)
    }

    private fun onQuickMoveDestinationNotFound(
        moveBundle: MoveBundle
    ) {
        (moveBundle.mode as? MoveMode.NotificationBased)?.let {
            cancelNotification(it.notificationResources)  // TODO
        }

        scope.launch {
            navigatorConfigDataSource.unsetQuickMoveDestination(
                fileType = moveBundle.file.fileType,
                sourceType = moveBundle.file.sourceType
            )

            moveFileNotificationManager.buildAndPostNotification(moveBundle.file)
        }
    }

    private fun onAutoMoveDestinationNotFound(moveBundle: MoveBundle) {
        scope.launch {
            navigatorConfigDataSource.unsetAutoMoveConfig(
                fileType = moveBundle.file.fileType,
                sourceType = moveBundle.file.sourceType
            )
            FileNavigator.reregisterFileObservers(context)
        }
        moveFileNotificationManager.buildAndPostNotification(moveBundle.file)
        autoMoveDestinationInvalidNotificationManager.buildAndPostNotification(
            fileAndSourceType = moveBundle.file.fileAndSourceType,
            autoMoveDestination = moveBundle.destination
        )
    }

    private suspend fun onSuccess(moveBundle: MoveBundle) {
        if (moveBundle.mode.showMoveResultToast) {
            context.showMoveSuccessToast(
                moveBundle = moveBundle
            )
        }

//        scope.launch {  // TODO
//            val movedFileDocumentUri =
//                moveBundle.destination.documentUri.childDocumentUri(fileName = moveBundle.file.mediaStoreFileData.name)
//            insertMoveEntryUseCase(
//                moveBundle.moveEntry(
//                    movedFileDocumentUri = movedFileDocumentUri,
//                    movedFileMediaUri = movedFileDocumentUri.mediaUri(context)!!,  // TODO
//                    dateTime = LocalDateTime.now(),
//                )
//            )
//        }
//        if (moveBundle.mode.updateLastMoveDestinations) {
//            scope.launch {
//                i { "Saving last move destination" }
//                navigatorConfigDataSource.saveQuickMoveDestination(
//                    fileType = moveBundle.file.fileType,
//                    sourceType = moveBundle.file.sourceType,
//                    destination = moveBundle.destination
//                )
//            }
//        }
    }
}

private suspend fun Context.showMoveSuccessToast(moveBundle: MoveBundle) {
    withContext(Dispatchers.Main) {
        showToast(
            resources.getText(
                id = if (moveBundle.mode.isAuto) R.string.auto_move_success_toast_text else R.string.move_success_toast_text,
                moveBundle.file.fileAndSourceType.label(
                    context = this@showMoveSuccessToast,
                    isGif = moveBundle.file.isGif
                ),
                moveBundle.destination.shortRepresentation(this@showMoveSuccessToast)
            )
        )
    }
}

private suspend fun Context.showMoveFailureToast(@StringRes explanationStringRes: Int) {
    withContext(Dispatchers.Main) {
        showToast(
            text = buildSpannedString {
                bold { append("${getString(R.string.couldnt_move)}: ") }
                append(getString(explanationStringRes))
            },
            duration = Toast.LENGTH_LONG
        )
    }
}
