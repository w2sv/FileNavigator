package com.w2sv.navigator.moving

import android.content.Context
import com.anggrayudi.storage.callback.FileCallback
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.hasChild
import com.w2sv.common.utils.isExternalStorageManger
import com.w2sv.kotlinutils.coroutines.firstBlocking
import com.w2sv.navigator.moving.model.MoveBundle
import com.w2sv.navigator.moving.model.MoveResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FileMover @Inject constructor(
    @GlobalScope(AppDispatcher.IO) private val scope: CoroutineScope
) {
    operator fun invoke(
        moveBundle: MoveBundle,
        context: Context
    ): MoveResult {
        // Exit if 'manage all files' permission not granted
        if (!isExternalStorageManger) {
            return MoveResult.Failure.ManageAllFilesPermissionMissing
        }

        if (!moveBundle.file.mediaStoreData.fileExists) {
            return MoveResult.Failure.MoveFileNotFound
        }

        // Exit on unsuccessful conversion to SimpleStorage objects
        val moveDestinationDocumentFile = moveBundle.destination.documentFile(context)
        val moveMediaFile = moveBundle.file.simpleStorageMediaFile(context)

        if (moveDestinationDocumentFile == null || moveMediaFile == null) {
            return MoveResult.Failure.InternalError
        }

        // Exit if file already at selected location.
        if (moveDestinationDocumentFile.hasChild(  // TODO: optimizable?
                context = context,
                path = moveBundle.file.mediaStoreData.name,
                requiresWriteAccess = false
            )
        ) {
            return MoveResult.Failure.FileAlreadyAtMoveDestination
        }

        val flow = MutableSharedFlow<MoveResult>()

        moveMediaFile.moveTo(
            targetFolder = moveDestinationDocumentFile,
            callback = object : FileCallback() {  // onReport override not called for some reason

                /**
                 * @param result androidx.documentfile.providerRawDocumentFile with uri of form "file:///storage/emulated/0/Moved/Screenshots/Screenshot_2024-07-03-17-02-26-344_com.w2sv.filenavigator.debug.jpg".
                 */
                override fun onCompleted(result: Any) {
                    scope.launch { flow.emit(MoveResult.Failure.InternalError) }
                }

                override fun onFailed(errorCode: ErrorCode) {
                    i { errorCode.toString() }

                    if (errorCode == ErrorCode.TARGET_FOLDER_NOT_FOUND && moveBundle.mode.isAuto) {
                        scope.launch {
                            flow.emit(
                                MoveResult.Failure.AutoMoveDestinationNotFound(
                                    moveBundle
                                )
                            )
                        }
                    } else {
                        context.showToast(errorCode.name)
                        scope.launch { flow.emit(MoveResult.Failure.InternalError) }  // TODO
                    }
                }
            }
        )

        return flow.firstBlocking()
    }
}