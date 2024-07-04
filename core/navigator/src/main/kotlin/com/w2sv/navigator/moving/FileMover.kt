package com.w2sv.navigator.moving

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.FileCallback
import com.w2sv.androidutils.res.getText
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.DocumentUri
import com.w2sv.common.utils.fileName
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.common.utils.showToast
import com.w2sv.core.navigator.R
import com.w2sv.navigator.FileNavigator
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import com.w2sv.navigator.notifications.NotificationResources
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FileMover @Inject constructor(
    private val moveResultFlow: MoveResultFlow,
    @GlobalScope(AppDispatcher.IO) private val scope: CoroutineScope
) {
    operator fun invoke(
        moveBundle: MoveBundle,
        context: Context
    ) {
        // Exit if 'manage all files' permission not granted
        if (!isExternalStorageManger) {
            return onMoveResult(MoveResult.Failure.ManageAllFilesPermissionMissing)
        }

        if (!moveBundle.file.mediaStoreData.fileExists) {
            return onMoveResult(MoveResult.Failure.MoveFileNotFound)
        }

        // Exit on unsuccessful conversion to SimpleStorage objects
        val moveDestinationDocumentFile = moveBundle.destination.documentFile(context)
        val moveMediaFile = moveBundle.file.simpleStorageMediaFile(context)

        if (moveDestinationDocumentFile == null || moveMediaFile == null) {
            return onMoveResult(MoveResult.Failure.InternalError)
        }

        // Exit if file already at selected location.
        if (moveDestinationDocumentFile.hasChild(  // TODO: optimizable?
                context = context,
                path = moveBundle.file.mediaStoreData.name,
                requiresWriteAccess = false
            )
        ) {
            return onMoveResult(MoveResult.Failure.FileAlreadyAtMoveDestination)
        }

        moveMediaFile.moveTo(
            targetFolder = moveDestinationDocumentFile,
            callback = object : FileCallback() {  // onReport override not called for some reason

                /**
                 * @param result androidx.documentfile.providerRawDocumentFile with uri of form "file:///storage/emulated/0/Moved/Screenshots/Screenshot_2024-07-03-17-02-26-344_com.w2sv.filenavigator.debug.jpg".
                 */
                override fun onCompleted(result: Any) {
                    onMoveResult(MoveResult.Success(moveBundle))
                }

                override fun onFailed(errorCode: ErrorCode) {
                    i { errorCode.toString() }

                    if (errorCode == ErrorCode.TARGET_FOLDER_NOT_FOUND && moveBundle.mode.isAuto) {
                        onMoveResult(MoveResult.Failure.AutoMoveDestinationNotFound(moveBundle))
                    } else {
                        context.showToast(errorCode.name)
                    }
                }
            }
        )
    }

    private fun onMoveResult(moveResult: MoveResult) {
        scope.launch { moveResultFlow.emit(moveResult) }
    }
}