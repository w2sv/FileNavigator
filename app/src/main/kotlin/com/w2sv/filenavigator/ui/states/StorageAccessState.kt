package com.w2sv.filenavigator.ui.states

import android.content.Context
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.data.model.FileType
import com.w2sv.data.model.StorageAccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

class StorageAccessState(
    private val priorStatus: StateFlow<StorageAccess>,
    private val setFileTypeStatuses: (Iterable<FileType>, newStatus: FileType.Status) -> Unit,
    private val saveStorageAccessStatus: (StorageAccess) -> Unit
) {
    private val status: StateFlow<StorageAccess> get() = _status
    private val _status = MutableStateFlow(StorageAccess.NoAccess)

    val anyAccessGranted: StateFlow<Boolean> =
        status.mapState { it != StorageAccess.NoAccess }

    fun updateStatus(context: Context) {
        _status.value = StorageAccess.get(context)
            .also { status ->
                if (status != priorStatus.value) {
                    i { "New manageExternalStoragePermissionGranted = $status diverting from previous = ${priorStatus.value}" }

                    when (status) {
                        StorageAccess.NoAccess -> setFileTypeStatuses(
                            FileType.getValues(),
                            FileType.Status.DisabledDueToNoFileAccess
                        )

                        StorageAccess.MediaFilesOnly -> {
                            setFileTypeStatuses(
                                FileType.NonMedia.getValues(),
                                FileType.Status.DisabledDueToMediaAccessOnly
                            )

                            if (priorStatus.value == StorageAccess.NoAccess) {
                                setFileTypeStatuses(FileType.Media.getValues(), FileType.Status.Enabled)
                            }
                        }

                        StorageAccess.AllFiles -> setFileTypeStatuses(
                            FileType.getValues(),
                            FileType.Status.Enabled
                        )
                    }

                    saveStorageAccessStatus(status)
                }
            }
    }
}