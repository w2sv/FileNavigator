package com.w2sv.filenavigator.ui.states

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.filenavigator.ui.screens.main.components.filetypeselection.defaultmovedestination.getDefaultMoveDestinationPath
import kotlinx.coroutines.flow.StateFlow

class DefaultMoveDestinationState(
    val destination: StateFlow<Uri?>,
    val saveDestination: (Uri?) -> Unit,
    private val context: Context
) {
    fun onDestinationSelected(treeUri: Uri?, context: Context) {
        if (treeUri != null) {
            DocumentFile.fromTreeUri(context, treeUri)?.let { documentFile ->
                saveDestination(documentFile.uri)
            }
        }
    }

    val isDestinationSet = destination.mapState { it != null }

    val destinationPath = destination.mapState {
        it?.let { nonNullDestination ->
            getDefaultMoveDestinationPath(nonNullDestination, context)
        }
    }
}