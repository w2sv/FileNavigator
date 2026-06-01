package com.w2sv.filenavigator.ui.screen.navigatorsettings.list

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.w2sv.common.util.takePersistableReadAndWriteUriPermission
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import com.w2sv.filenavigator.ui.LocalMoveDestinationLabelProvider

@Composable
fun rememberAutoMoveDestinationPath(
    destination: LocalDestinationApi?,
    context: Context = LocalContext.current,
    moveDestinationLabelProvider: MoveDestinationLabelProvider = LocalMoveDestinationLabelProvider.current
): State<String?> =
    remember(destination) {
        mutableStateOf(destination?.let { moveDestinationLabelProvider.invoke(it, context) })
    }

@Composable
fun rememberSelectAutoMoveDestination(onDestinationSelected: (LocalDestinationApi) -> Unit): ManagedActivityResultLauncher<Uri?, Uri?> {
    val context: Context = LocalContext.current
    return rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { optionalTreeUri ->
        optionalTreeUri?.let { treeUri ->
            context.contentResolver.takePersistableReadAndWriteUriPermission(treeUri)
            onDestinationSelected(
                LocalDestination.fromTreeUri(
                    context = context,
                    treeUri = treeUri
                )!! // TODO: null case possible?
            )
        }
    }
}
