package com.w2sv.filenavigator.ui.states

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.data.model.FileType
import com.w2sv.data.storage.repositories.FileTypeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DefaultMoveDestinationState(
    private val fileTypeRepository: FileTypeRepository,
    private val scope: CoroutineScope,
    context: Context
) {
    val pathMap =
        fileTypeRepository
            .defaultDestinationMap
            .mapValues { (_, destinationFlow) ->
                destinationFlow
                    .stateIn(
                        scope,
                        SharingStarted.Eagerly,
                        null
                    )
                    .mapState { uri ->
                        uri?.let { getDefaultMoveDestinationPath(it, context) }
                    }
            }

    // ==================
    // Configuration
    // ==================

    val selectionSource = MutableStateFlow<FileType.Source?>(null)

    fun onDestinationSelected(treeUri: Uri, context: Context) {
        DocumentFile.fromTreeUri(context, treeUri)?.let { documentFile ->
            saveDestination(selectionSource.value!!, documentFile.uri)
        }
        selectionSource.value = null
    }

    fun saveDestination(source: FileType.Source, destination: Uri?) {
        scope.launch {
            fileTypeRepository.saveDefaultDestination(source, destination)
        }
    }
}

private fun getDefaultMoveDestinationPath(uri: Uri, context: Context): String? =
    DocumentFile.fromSingleUri(context, uri)?.getSimplePath(context)