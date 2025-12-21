package com.w2sv.filenavigator.ui.screen.navigatorsettings.configlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import com.w2sv.domain.usecase.PreviewMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.LocalMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.designsystem.FileTypeIcon
import com.w2sv.filenavigator.ui.designsystem.MoreIconButtonWithDropdownMenu
import com.w2sv.filenavigator.ui.designsystem.SubDirectoryIcon
import com.w2sv.filenavigator.ui.modelext.color
import com.w2sv.filenavigator.ui.modelext.stringResource
import com.w2sv.filenavigator.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

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

@Preview
@Composable
private fun FileTypeAccordionPrev() {
    AppTheme {
        val imageFileType = PresetFileType.Image.toDefaultFileType()
        CompositionLocalProvider(LocalMoveDestinationPathConverter provides PreviewMoveDestinationPathConverter()) {
            FileTypeAccordion(
                fileType = imageFileType,
                setSourceAutoMoveConfigs = {},
                sourceTypeConfigMap = NavigatorConfig.default.fileTypeConfig(imageFileType).sourceTypeConfigMap.toImmutableMap(),
                onSourceCheckedChange = { _, _ -> },
                setSourceAutoMoveConfig = { _, _ -> },
                showFileTypeConfigurationDialog = {}
            )
        }
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
        FileTypeIcon(
            fileType = fileType,
            tint = fileType.color,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(34.dp)
        )
        Text(
            text = fileType.stringResource(),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        setSourceAutoMoveConfigs?.let {
            MoreIconButtonWithDropdownMenu {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.set_auto_move_destination_for_all_sources)) },
                    onClick = {
                        collapseMenu()
                        setSourceAutoMoveConfigs()
                    },
                    leadingIcon = { SubDirectoryIcon() }
                )
            }
        }
        IconButton(onClick = { showFileTypeConfigurationDialog(fileType) }) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.open_the_file_type_configuration_dialog)
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
