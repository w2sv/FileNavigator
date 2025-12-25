package com.w2sv.filenavigator.ui.screen.navigatorsettings.list

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.util.takePersistableReadAndWriteUriPermission
import com.w2sv.core.common.R
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.filenavigator.ui.LocalMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.designsystem.IconSize
import com.w2sv.filenavigator.ui.designsystem.SubDirectoryIcon

@Composable
fun rememberAutoMoveDestinationPath(
    destination: LocalDestinationApi?,
    context: Context = LocalContext.current,
    moveDestinationPathConverter: MoveDestinationPathConverter = LocalMoveDestinationPathConverter.current
): State<String?> =
    remember(destination) {
        mutableStateOf(
            destination?.let { moveDestinationPathConverter.invoke(it, context) }
        )
    }

@Composable
fun AutoMoveRow(
    destinationPath: String,
    changeDestination: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(value = LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .padding(start = 10.dp, bottom = 4.dp)
        ) {
            SubDirectoryIcon(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(20.dp)
            )
            Text(destinationPath, modifier = Modifier.weight(1f), fontSize = 14.sp)
            IconButton(
                onClick = { changeDestination() },
                modifier = Modifier.size(IconSize.Big)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_folder_edit_24),
                    contentDescription = stringResource(R.string.select_the_auto_move_destination),
                    modifier = Modifier.size(IconSize.Big),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
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
