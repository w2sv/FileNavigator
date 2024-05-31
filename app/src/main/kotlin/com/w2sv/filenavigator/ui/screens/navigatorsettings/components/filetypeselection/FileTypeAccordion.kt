package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.w2sv.common.utils.getDocumentUriPath
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.drawer.FileTypeIcon
import com.w2sv.filenavigator.ui.model.color

@Composable
fun FileTypeAccordion(
    fileType: FileType,
    excludeFileType: () -> Unit,
    mediaFileSourceEnabled: (FileType.Source) -> Boolean,
    onMediaFileSourceCheckedChange: (FileType.Source, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Header(
            fileType = fileType,
            excludeFileType = excludeFileType
        )
        FileTypeSourcesSurface(
            fileType = fileType,
            mediaFileSourceEnabled = mediaFileSourceEnabled,
            setMediaFileSourceEnabled = onMediaFileSourceCheckedChange
        )
    }
}

@Composable
private fun rememberSelectAutoMoveDestination(onDestinationSelected: (Uri) -> Unit): ManagedActivityResultLauncher<Uri?, Uri?> {
    val context: Context = LocalContext.current
    return rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { optionalTreeUri ->
        optionalTreeUri?.let {
            onDestinationSelected(
                DocumentFile.fromTreeUri(context, it)!!.uri
            )
        }
    }
}

@Composable
private fun Header(
    fileType: FileType,
    excludeFileType: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var autoMoveEnabled by rememberSaveable(fileType) {
        mutableStateOf(false)
    }
    var autoMoveDestination by remember {
        mutableStateOf<Uri?>(null)
    }
    val selectAutoMoveDestination = rememberSelectAutoMoveDestination {
        autoMoveEnabled = true
        autoMoveDestination = it
    }
    val context: Context = LocalContext.current
    val autoMovePath by remember(autoMoveDestination) {
        mutableStateOf(
            autoMoveDestination?.let { getDocumentUriPath(it, context) }
        )
    }
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column {
            FileTypeRow(
                fileType = fileType,
                excludeFileType = excludeFileType,
                autoMoveEnabled = autoMoveEnabled,
                onAutoMoveEnabledSwitchCheckedChange = {
                    if (it && autoMoveDestination == null) {
                        selectAutoMoveDestination.launch(null)
                    } else {
                        autoMoveEnabled = it
                    }
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            AnimatedVisibility(visible = autoMoveEnabled && autoMovePath != null) {
                AutoMoveRow(
                    destinationPath = autoMovePath!!,
                    changeDestination = { selectAutoMoveDestination.launch(autoMoveDestination) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FileTypeRow(
    fileType: FileType,
    excludeFileType: () -> Unit,
    autoMoveEnabled: Boolean,
    onAutoMoveEnabledSwitchCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        FileTypeIcon(
            fileType = fileType,
            tint = fileType.color,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(34.dp)
        )
        Text(
            text = stringResource(id = fileType.titleRes),
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(id = R.string.auto_move),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp,
            modifier = Modifier.padding(end = 4.dp)
        )
        Switch(
            checked = autoMoveEnabled,
            onCheckedChange = onAutoMoveEnabledSwitchCheckedChange,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
        VerticalDivider(
            modifier = Modifier
                .height(32.dp)
                .padding(horizontal = 4.dp)
        )
        Text(
            text = stringResource(R.string.exclude),
            modifier = Modifier
                .padding(end = 6.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { excludeFileType() }
                .padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AutoMoveRow(
    destinationPath: String,
    changeDestination: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, bottom = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_subdirectory_arrow_right_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(20.dp)
            )
            Text(destinationPath, modifier = Modifier.weight(1f), fontSize = 14.sp)
            IconButton(
                onClick = { changeDestination() },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_configure_folder_24),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

//@Preview
//@Composable
//private fun AutoMoveRowPrev() {
//    AppTheme {
//        Surface(
//            tonalElevation = 2.dp,
//            shape = MaterialTheme.shapes.small,
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            AutoMoveRow(destinationPath = "/path/component/somefolder")
//        }
//    }
//}

@Composable
private fun AddAutoMoveDestinationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contentDescription = stringResource(R.string.add_auto_move_destination)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
            .semantics {
                this.contentDescription = contentDescription
            }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_new_folder_24),
            contentDescription = null,
        )
        Text(text = stringResource(R.string.auto), fontSize = 12.sp)
    }
}