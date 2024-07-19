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
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveMode
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.managers.AutoMoveDestinationInvalidNotificationManager
import com.w2sv.navigator.notifications.managers.NewMoveFileNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i
import java.time.LocalDateTime
import javax.inject.Inject

internal class MoveResultListener @Inject constructor(
    private val insertMoveEntryUseCase: InsertMoveEntryUseCase,
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val newMoveFileNotificationManager: NewMoveFileNotificationManager,
    private val autoMoveDestinationInvalidNotificationManager: AutoMoveDestinationInvalidNotificationManager,
    @GlobalScope(AppDispatcher.IO) private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) {
    fun onPreMoveCancellation(
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

    operator fun invoke(
        moveBundle: MoveBundle,
        moveResult: MoveResult
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
                if (moveResult == MoveResult.Failure.MoveDestinationNotFound) {
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
                            invoke(
                                moveBundle = moveBundle,
                                moveResult = MoveResult.Failure.InternalError
                            )
                        }

                        else -> Unit  // TODO
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
            navigatorConfigDataSource.unsetLastMoveDestination(
                fileType = moveBundle.file.fileType,
                sourceType = moveBundle.file.sourceType
            )

            newMoveFileNotificationManager.buildAndPost(moveBundle.file)
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
        newMoveFileNotificationManager.buildAndPost(moveBundle.file)
        autoMoveDestinationInvalidNotificationManager.buildAndPost(
            fileAndSourceType = moveBundle.file.fileAndSourceType,
            autoMoveDestination = moveBundle.destination
        )
    }

    private fun onSuccess(moveBundle: MoveBundle) {
        if (moveBundle.mode.showMoveResultToast) {
            context.showMoveSuccessToast(
                moveBundle = moveBundle
            )
        }

        scope.launch {
            val movedFileDocumentUri =
                moveBundle.destination.documentUri.childDocumentUri(fileName = moveBundle.file.mediaStoreData.name)
            insertMoveEntryUseCase(
                moveBundle.moveEntry(
                    movedFileDocumentUri = movedFileDocumentUri,
                    movedFileMediaUri = movedFileDocumentUri.mediaUri(context)!!,
                    dateTime = LocalDateTime.now(),
                )
            )
        }
        if (moveBundle.mode.updateLastMoveDestinations) {
            scope.launch {
                i { "Saving last move destination" }
                navigatorConfigDataSource.saveLastMoveDestination(
                    fileType = moveBundle.file.fileType,
                    sourceType = moveBundle.file.sourceType,
                    destination = moveBundle.destination
                )
            }
        }
    }
}

private fun Context.showMoveSuccessToast(moveBundle: MoveBundle) {
    showToast(
        resources.getText(
            id = if (moveBundle.mode.isAuto) R.string.auto_move_success_toast_text else R.string.move_success_toast_text,
            moveBundle.file.fileAndSourceType.label(context = this, isGif = moveBundle.file.isGif),
            moveBundle.destination.shortRepresentation(this)
        )
    )
}

private fun Context.showMoveFailureToast(@StringRes explanationStringRes: Int) {
    showToast(
        text = buildSpannedString {
            bold { append("${getString(R.string.couldnt_move)}: ") }
            append(getString(explanationStringRes))
        },
        duration = Toast.LENGTH_LONG
    )
}
