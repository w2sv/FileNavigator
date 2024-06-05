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
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.w2sv.common.utils.getDocumentUriPath
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.TweakedSwitch
import com.w2sv.filenavigator.ui.designsystem.drawer.FileTypeIcon
import com.w2sv.filenavigator.ui.model.color

@Composable
fun FileTypeAccordion(
    fileType: FileType,
    excludeFileType: () -> Unit,
    autoMoveConfig: AutoMoveConfig,
    setAutoMoveConfig: (AutoMoveConfig) -> Unit,
    mediaFileSourceEnabled: (SourceType) -> Boolean,
    onMediaFileSourceCheckedChange: (SourceType, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Header(
            fileType = fileType,
            excludeFileType = excludeFileType,
            autoMoveConfig = autoMoveConfig,
            setAutoMoveConfig = setAutoMoveConfig
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
    autoMoveConfig: AutoMoveConfig,
    setAutoMoveConfig: (AutoMoveConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectAutoMoveDestination = rememberSelectAutoMoveDestination(
        onDestinationSelected = {
            setAutoMoveConfig(AutoMoveConfig(enabled = true, destination = it))
        }
    )
    val context: Context = LocalContext.current
    val autoMovePath by remember(autoMoveConfig.destination) {
        mutableStateOf(
            autoMoveConfig.destination?.let { getDocumentUriPath(it, context) }
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
                autoMoveEnabled = autoMoveConfig.enabled,
                onAutoMoveEnabledSwitchCheckedChange = {
                    if (it && autoMoveConfig.destination == null) {
                        selectAutoMoveDestination.launch(null)
                    } else {
                        setAutoMoveConfig(autoMoveConfig.copy(enabled = it))
                    }
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            AnimatedVisibility(visible = autoMoveConfig.enabled && autoMovePath != null) {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                    HorizontalDivider()
                    AutoMoveRow(
                        destinationPath = autoMovePath!!,
                        changeDestination = { selectAutoMoveDestination.launch(autoMoveConfig.destination) },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
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
            modifier = Modifier.padding(end = 2.dp)
        )
        TweakedSwitch(
            checked = autoMoveEnabled,
            onCheckedChange = onAutoMoveEnabledSwitchCheckedChange,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
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
    CompositionLocalProvider(value = LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .padding(start = 10.dp, bottom = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_subdirectory_arrow_right_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(20.dp)
            )
            Text(destinationPath, modifier = Modifier.weight(1f), fontSize = 14.sp)
            IconButton(
                onClick = { changeDestination() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_folder_edit_24),
                    contentDescription = stringResource(R.string.select_the_auto_move_destination),
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
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