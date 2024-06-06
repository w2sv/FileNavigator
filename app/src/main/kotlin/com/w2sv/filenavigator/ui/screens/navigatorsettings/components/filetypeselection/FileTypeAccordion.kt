package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.drawer.FileTypeIcon
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.rememberSelectAutoMoveDestination
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun FileTypeAccordion(
    fileType: FileType,
    excludeFileType: () -> Unit,
    setSourceAutoMoveConfigs: (AutoMoveConfig) -> Unit,
    sourceTypeConfigMap: ImmutableMap<SourceType, SourceConfig>,
    onSourceCheckedChange: (SourceType, Boolean) -> Unit,
    setSourceAutoMoveConfig: (SourceType, AutoMoveConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Header(
            fileType = fileType,
            excludeFileType = excludeFileType,
            setSourceAutoMoveConfigs = setSourceAutoMoveConfigs
        )
        FileTypeSourcesSurface(
            fileType = fileType,
            sourceTypeConfigMap = sourceTypeConfigMap,
            onSourceCheckedChange = onSourceCheckedChange,
            setSourceAutoMoveConfig = setSourceAutoMoveConfig,
        )
    }
}

@Composable
private fun Header(
    fileType: FileType,
    excludeFileType: () -> Unit,
    setSourceAutoMoveConfigs: (AutoMoveConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectAutoMoveDestination = rememberSelectAutoMoveDestination(
        onDestinationSelected = {
            setSourceAutoMoveConfigs(AutoMoveConfig(enabled = true, destination = it))
        }
    )

    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        FileTypeRow(
            fileType = fileType,
            excludeFileType = excludeFileType,
            setSourceAutoMoveConfigs = { selectAutoMoveDestination.launch(null) }
        )
    }
}

@Composable
private fun FileTypeRow(
    fileType: FileType,
    excludeFileType: () -> Unit,
    setSourceAutoMoveConfigs: () -> Unit,
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
        var expanded by rememberSaveable {
            mutableStateOf(false)
        }
        Box {
            IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(36.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(text = "Set Auto Move destination for all sources") },
                    onClick = {
                        expanded = false
                        setSourceAutoMoveConfigs()
                    }
                )
            }
        }
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