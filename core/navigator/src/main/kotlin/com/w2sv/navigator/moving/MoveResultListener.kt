package com.w2sv.navigator.moving

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.w2sv.androidutils.res.getText
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.fileName
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
import javax.inject.Singleton

@Singleton
internal class MoveResultListener @Inject constructor(
    private val insertMoveEntryUseCase: InsertMoveEntryUseCase,
    private val navigatorConfigDataSource: NavigatorConfigDataSource,
    private val newMoveFileNotificationManager: NewMoveFileNotificationManager,
    private val autoMoveDestinationInvalidNotificationManager: AutoMoveDestinationInvalidNotificationManager,
    @GlobalScope(AppDispatcher.IO) private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) {
    operator fun invoke(
        moveResult: MoveResult,
        notificationResources: NotificationResources? = null,
        showToast: Boolean = true
    ) {
        if (moveResult.cancelNotification) {
            cancelNotification(notificationResources)
        }
        when (moveResult) {
            is MoveResult.Success -> {
                onSuccess(moveResult.moveBundle, showToast = showToast)
            }

            is MoveResult.Failure.Generic -> {
                if (showToast) {
                    context.showMoveFailureToast(moveResult.explanationStringRes)
                }
            }

            is MoveResult.Failure.MoveDestinationNotFound -> {
                when (moveResult.moveBundle.mode) {
                    MoveMode.Auto -> {
                        onAutoMoveDestinationNotFound(moveResult.moveBundle)
                    }

                    MoveMode.Quick -> {
                        onQuickMoveDestinationNotFound(
                            moveBundle = moveResult.moveBundle,
                            notificationResources = notificationResources,
                            showToast = showToast
                        )
                    }

                    MoveMode.ManualSelection -> {  // Shouldn't normally occur
                        invoke(MoveResult.Failure.InternalError, notificationResources)
                    }
                }
            }
        }
    }

    private fun cancelNotification(notificationResources: NotificationResources?) {
        notificationResources?.cancelNotification(context)
    }

    private fun onQuickMoveDestinationNotFound(
        moveBundle: MoveBundle,
        notificationResources: NotificationResources?,
        showToast: Boolean
    ) {
        cancelNotification(notificationResources)  // TODO
        if (showToast) {
            context.showMoveFailureToast(R.string.quick_move_destination_doesnt_exist)
        }
        scope.launch {
            navigatorConfigDataSource.unsetLastMoveDestination(
                fileType = moveBundle.file.fileType,
                sourceType = moveBundle.file.sourceType
            )
        }
        newMoveFileNotificationManager.buildAndPost(moveBundle.file)
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

    private fun onSuccess(moveBundle: MoveBundle, showToast: Boolean) {
        if (showToast) {
            context.showMoveSuccessToast(
                moveBundle = moveBundle
            )
        }

        scope.launch {
            val movedFileDocumentUri = movedFileDocumentUri(
                moveDestinationDocumentUri = moveBundle.destination,
                fileName = moveBundle.file.mediaStoreData.name
            )
            insertMoveEntryUseCase(
                moveBundle.moveEntry(
                    movedFileDocumentUri = movedFileDocumentUri,
                    movedFileMediaUri = movedFileDocumentUri.mediaUri(context)!!,
                    dateTime = LocalDateTime.now(),
                )
            )

            if (moveBundle.mode.updateLastMoveDestinations) {
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

private fun movedFileDocumentUri(
    moveDestinationDocumentUri: DocumentUri,
    fileName: String
): DocumentUri =
    DocumentUri.parse("$moveDestinationDocumentUri%2F${Uri.encode(fileName)}")

private fun Context.showMoveSuccessToast(moveBundle: MoveBundle) {
    showToast(
        resources.getText(
            id = if (moveBundle.mode.isAuto) R.string.auto_move_success_toast_text else R.string.move_success_toast_text,
            moveBundle.file.fileAndSourceType.label(context = this, isGif = moveBundle.file.isGif),
            shortMoveDestinationRepresentation(moveBundle.destination, this)
        )
    )
}

internal fun shortMoveDestinationRepresentation(
    moveDestination: DocumentUri,
    context: Context
): String =
    "/${moveDestination.documentFile(context)!!.fileName(context)}"

private fun Context.showMoveFailureToast(@StringRes explanationStringRes: Int) {
    showToast(
        text = buildSpannedString {
            bold { append("${getString(R.string.couldnt_move)}: ") }
            append(getString(explanationStringRes))
        },
        duration = Toast.LENGTH_LONG
    )
}
