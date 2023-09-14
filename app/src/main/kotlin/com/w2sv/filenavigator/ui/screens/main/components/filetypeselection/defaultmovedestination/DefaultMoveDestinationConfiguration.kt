package com.w2sv.filenavigator.ui.screens.main.components.filetypeselection.defaultmovedestination

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStateFlow
import com.w2sv.androidutils.ui.unconfirmed_state.UnconfirmedStatesComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DefaultMoveDestinationConfiguration(
    val moveDestination: UnconfirmedStateFlow<Uri?>,
    val isLocked: UnconfirmedStateFlow<Boolean>,
    private val scope: CoroutineScope
) : UnconfirmedStatesComposition(
    unconfirmedStates = listOf(moveDestination, isLocked),
    coroutineScope = scope
) {
    fun onMoveDestinationSelected(treeUri: Uri?, context: Context) {
        if (treeUri != null) {
            DocumentFile.fromTreeUri(context, treeUri)?.let { documentFile ->
                moveDestination.value = documentFile.uri
            }
        }
    }

    fun launchSync(): Job =
        scope.launch {
            sync()
        }
}