package com.w2sv.filenavigator.ui.states

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.common.utils.getDocumentUriPath
import com.w2sv.data.model.FileType
import com.w2sv.data.storage.repositories.FileTypeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DefaultMoveDestinationState(
    private val fileTypeRepository: FileTypeRepository,
    private val scope: CoroutineScope,
    context: Context
) {
    val pathMap =
        fileTypeRepository
            .defaultDestinationStateFlowMap
            .mapValues { (_, destinationStateFlow) ->
                destinationStateFlow.mapState { uri ->
                    uri?.let { getDocumentUriPath(it, context) }
                }
            }

    // ==================
    // Configuration
    // ==================

    fun launchPickerFor(source: FileType.Source) {
        pickerSource.value = source
    }

    val launchPicker get() = _launchPicker.asSharedFlow()
    private val _launchPicker = MutableSharedFlow<Unit>()

    private val pickerSource = MutableStateFlow<FileType.Source?>(null)
        .apply {
            scope.launch {
                collect {
                    if (it != null) {
                        _launchPicker.emit(Unit)
                    }
                }
            }
        }

    fun onDestinationSelected(treeUri: Uri, context: Context) {
        DocumentFile.fromTreeUri(context, treeUri)?.let { documentFile ->
            saveDestination(pickerSource.value!!, documentFile.uri)
        }
        pickerSource.value = null
    }

    fun saveDestination(source: FileType.Source, destination: Uri?) {
        scope.launch {
            fileTypeRepository.saveDefaultDestination(source, destination)
        }
    }
}