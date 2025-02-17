package com.w2sv.navigator.moving

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.androidutils.res.getHtmlFormattedText
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.core.navigator.R
import com.w2sv.domain.repository.NavigatorConfigDataSource
import com.w2sv.domain.usecase.InsertMovedFileUseCase
import com.w2sv.navigator.moving.model.AnyMoveBundle
import com.w2sv.navigator.moving.model.DestinationSelectionManner
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources
import com.w2sv.navigator.notifications.appnotifications.AutoMoveDestinationInvalidNotificationManager
import com.w2sv.navigator.notifications.appnotifications.movefile.MoveFileNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import slimber.log.i

internal class MoveResultListener @Inject constructor(
    private val insertMovedFileUseCase: InsertMovedFileUseCase,
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val moveFileNotificationManager: MoveFileNotificationManager,
    private val autoMoveDestinationInvalidNotificationManager: AutoMoveDestinationInvalidNotificationManager,
    @GlobalScope(AppDispatcher.IO) private val scope: CoroutineScope, // TODO
    @ApplicationContext private val context: Context
) {
    suspend fun onMoveResult(moveResultBundle: MoveResult.Bundle) {
        i { "Received $moveResultBundle" }

        when (moveResultBundle) {
            is MoveResult.Bundle.PreCheckFailure -> {
                moveResultBundle.onPreCheckFailure()
            }

            is MoveResult.Bundle.PostMoveBundleCreation -> {
                moveResultBundle.onResult()
            }
        }
    }

    private suspend fun MoveResult.Bundle.PreCheckFailure.onPreCheckFailure() {
        if (moveFailure.cancelMoveFileNotification && notificationResources != null) {
            cancelMoveFileNotification(notificationResources)
        }

        moveFailure.explanationStringRes?.let {
            context.showMoveFailureToast(it)
        }
    }

    private suspend fun MoveResult.Bundle.PostMoveBundleCreation.onResult() {
        if (moveResult.cancelMoveFileNotification) {
            (moveBundle.destinationSelectionManner as? DestinationSelectionManner.NotificationBased)?.let {
                cancelMoveFileNotification(it.notificationResources)
            }
        }
        when (moveResult) {
            is MoveResult.Success -> {
                moveBundle.onSuccess()
            }

            is MoveResult.Failure -> {
                if (moveResult.explanationStringRes != null && moveBundle.notifyAboutMoveResult) {
                    context.showMoveFailureToast(moveResult.explanationStringRes)
                }
                if (moveResult == MoveResult.MoveDestinationNotFound) {
                    when (moveBundle) {
                        is MoveBundle.AutoMove -> {
                            moveBundle.onDestinationNotFound()
                        }

                        is MoveBundle.QuickMove -> {
                            moveBundle.onDestinationNotFound()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun cancelMoveFileNotification(notificationResources: NotificationResources?) {
        notificationResources?.cancelNotification(context)
    }

    private fun MoveBundle.QuickMove.onDestinationNotFound() {
        (destinationSelectionManner as? DestinationSelectionManner.NotificationBased)?.let {
            cancelMoveFileNotification(it.notificationResources)
        }

        scope.launch {
            navigatorConfigDataSource.unsetQuickMoveDestination(
                fileType = file.fileType,
                sourceType = file.sourceType
            )

            moveFileNotificationManager.buildAndPostNotification(file)
        }
    }

    private fun MoveBundle.AutoMove.onDestinationNotFound() {
        scope.launch {
            navigatorConfigDataSource.unsetAutoMoveConfig(
                fileType = file.fileType,
                sourceType = file.sourceType
            )
        }
        moveFileNotificationManager.buildAndPostNotification(file)
        autoMoveDestinationInvalidNotificationManager.buildAndPostNotification(
            fileAndSourceType = file.fileAndSourceType,
            autoMoveDestination = destination
        )
    }

    private suspend fun AnyMoveBundle.onSuccess() {
        if (notifyAboutMoveResult) {
            context.showMoveSuccessToast(
                moveBundle = this
            )
        }

        scope.launch {
            insertMovedFileUseCase(
                movedFileEntry(
                    context = context,
                    dateTime = LocalDateTime.now()
                )
            )
        }
        if (destinationSelectionManner.isPicked) {
            destination.quickMoveDestination?.let {
                i { "Saving quick move destination $it" }

                scope.launch {
                    navigatorConfigDataSource.saveQuickMoveDestination(
                        fileType = file.fileType,
                        sourceType = file.sourceType,
                        destination = it
                    )
                }
            }
        }
    }
}

private suspend fun Context.showMoveSuccessToast(moveBundle: AnyMoveBundle) {
    withContext(Dispatchers.Main) {
        showToast(
            resources.getHtmlFormattedText(
                id = if (moveBundle.destinationSelectionManner.isAuto) {
                    R.string.auto_move_success_toast_text
                } else {
                    R.string.move_success_toast_text
                },
                moveBundle.file.fileAndSourceType.label(
                    context = this@showMoveSuccessToast,
                    isGif = moveBundle.file.isGif
                ),
                moveBundle.destination.uiRepresentation(this@showMoveSuccessToast)
            )
        )
    }
}

private val AnyMoveBundle.notifyAboutMoveResult: Boolean
    get() = (this as? MoveBundle.Batchable)?.batched != true

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
