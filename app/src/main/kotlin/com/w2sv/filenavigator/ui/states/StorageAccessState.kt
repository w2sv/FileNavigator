package com.w2sv.filenavigator.ui.states

import android.content.Context
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.data.model.FileType
import com.w2sv.data.model.StorageAccessStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import slimber.log.i

class StorageAccessState(
    private val priorStatus: StateFlow<StorageAccessStatus>,
    private val setFileTypeStatuses: (Iterable<FileType>, newStatus: FileType.Status) -> Unit,
    private val saveStorageAccessStatus: (StorageAccessStatus) -> Unit
) {
    private val status: StateFlow<StorageAccessStatus> get() = _status
    private val _status = MutableStateFlow(StorageAccessStatus.NoAccess)

    val anyAccessGranted: StateFlow<Boolean> =
        status.mapState { it != StorageAccessStatus.NoAccess }

    fun updateStatus(context: Context) {
        _status.value = StorageAccessStatus.get(context)
            .also { status ->
                if (status != priorStatus.value) {
                    i { "New manageExternalStoragePermissionGranted = $status diverting from previous = $priorStatus" }

                    when (status) {
                        StorageAccessStatus.NoAccess -> setFileTypeStatuses(
                            FileType.values,
                            FileType.Status.DisabledDueToNoFileAccess
                        )

                        StorageAccessStatus.MediaFilesOnly -> {
                            setFileTypeStatuses(
                                FileType.NonMedia.all,
                                FileType.Status.DisabledDueToMediaAccessOnly
                            )

                            if (priorStatus.value == StorageAccessStatus.NoAccess) {
                                setFileTypeStatuses(FileType.Media.all, FileType.Status.Enabled)
                            }
                        }

                        StorageAccessStatus.AllFiles -> setFileTypeStatuses(
                            FileType.values,
                            FileType.Status.Enabled
                        )
                    }

                    saveStorageAccessStatus(status)
                }
            }
    }
}