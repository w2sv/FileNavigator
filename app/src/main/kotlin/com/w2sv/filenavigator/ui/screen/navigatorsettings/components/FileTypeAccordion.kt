package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import com.w2sv.filenavigator.ui.designsystem.FileTypeIcon
import com.w2sv.filenavigator.ui.designsystem.MoreIconButtonWithDropdownMenu
import com.w2sv.filenavigator.ui.modelext.color
import com.w2sv.filenavigator.ui.modelext.stringResource
import com.w2sv.filenavigator.ui.theme.AppTheme
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
        Box(contentAlignment = Alignment.Center) {
            FileTypeIcon(
                fileType = fileType,
                tint = fileType.color,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(34.dp)
            )
            IconButton(
                onClick = { showFileTypeConfigurationDialog(fileType) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset((-2).dp, 10.dp)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.open_the_file_type_configuration_dialog)
                )
            }
        }
        Text(
            text = fileType.stringResource(),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        setSourceAutoMoveConfigs?.let {
            MoreIconButtonWithDropdownMenu(modifier = Modifier.padding(end = 6.dp)) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.set_auto_move_destination_for_all_sources)) },
                    onClick = {
                        collapseMenu()
                        setSourceAutoMoveConfigs()
                    },
                    leadingIcon = {
                        SubDirectoryIcon()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun HeaderPrev() {
    AppTheme {
        Header(PresetFileType.Image.toDefaultFileType(), {}, {})
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
