package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.FileTypeIcon
import com.w2sv.filenavigator.ui.modelext.color
import com.w2sv.filenavigator.ui.modelext.stringResource
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun FileTypeAccordion(
    fileType: FileType,
    setSourceAutoMoveConfigs: ((AutoMoveConfig) -> Unit)?,
    sourceTypeConfigMap: ImmutableMap<SourceType, SourceConfig>,
    onSourceCheckedChange: (SourceType, Boolean) -> Unit,
    setSourceAutoMoveConfig: (SourceType, AutoMoveConfig) -> Unit,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Header(
            fileType = fileType,
            setSourceAutoMoveConfigs = setSourceAutoMoveConfigs,
            showFileTypeConfigurationDialog = showFileTypeConfigurationDialog
        )
        SourcesSurface(
            fileType = fileType,
            sourceTypeConfigMap = sourceTypeConfigMap,
            onSourceCheckedChange = onSourceCheckedChange,
            setSourceAutoMoveConfig = setSourceAutoMoveConfig
        )
    }
}

@Composable
private fun Header(
    fileType: FileType,
    setSourceAutoMoveConfigs: ((AutoMoveConfig) -> Unit)?,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectAutoMoveDestination = setSourceAutoMoveConfigs?.let { nonNullSetSourceAutoMoveConfigs ->
        rememberSelectAutoMoveDestination(
            onDestinationSelected = { destination ->
                nonNullSetSourceAutoMoveConfigs(AutoMoveConfig(enabled = true, destination = destination))
            }
        )
    }

    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        FileTypeRow(
            fileType = fileType,
            setSourceAutoMoveConfigs = selectAutoMoveDestination?.let { { it.launch(null) } },
            showFileTypeConfigurationDialog = showFileTypeConfigurationDialog
        )
    }
}

@Composable
private fun FileTypeRow(
    fileType: FileType,
    setSourceAutoMoveConfigs: (() -> Unit)?,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box {
            FileTypeIcon(
                fileType = fileType,
                tint = fileType.color,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(34.dp)
            )
        }
        Text(
            text = fileType.stringResource(),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        setSourceAutoMoveConfigs?.let {
            MoreIconButtonWithDropdownMenu(setSourceAutoMoveConfigs = it)
        }
        IconButton(onClick = { showFileTypeConfigurationDialog(fileType) }) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun MoreIconButtonWithDropdownMenu(setSourceAutoMoveConfigs: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_vert_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.border(
                width = Dp.Hairline,
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.extraSmall
            )
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.set_auto_move_destination_for_all_sources)) },
                onClick = {
                    expanded = false
                    setSourceAutoMoveConfigs()
                },
                leadingIcon = {
                    SubDirectoryIcon()
                }
            )
        }
    }
}

// @Preview
// @Composable
// private fun AutoMoveRowPrev() {
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
// }
