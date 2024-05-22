package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.drawer.FileTypeIcon
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.theme.onSurfaceDisabled
import com.w2sv.filenavigator.ui.utils.orOnSurfaceDisabledIf

@Composable
fun FileTypeAccordion(
    fileType: FileType,
    isEnabled: Boolean,
    isFirstDisabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    mediaFileSourceEnabled: (FileType.Source) -> Boolean,
    onMediaFileSourceCheckedChange: (FileType.Source, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        if (isFirstDisabled) {
            Text(
                text = stringResource(R.string.disabled),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceDisabled,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Header(
            fileType = fileType,
            isEnabled = isEnabled,
            onCheckedChange = onCheckedChange
        )
        AnimatedVisibility(
            visible = isEnabled
        ) {
            FileTypeSourcesSurface(
                fileType = fileType,
                mediaFileSourceEnabled = mediaFileSourceEnabled,
                setMediaFileSourceEnabled = onMediaFileSourceCheckedChange
            )
        }
    }
}

@Composable
private fun Header(
    fileType: FileType,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAutoMoveRow by rememberSaveable {
        mutableStateOf(isEnabled)
    }
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FileTypeIcon(
                    fileType = fileType,
                    tint = fileType.color.orOnSurfaceDisabledIf(condition = !isEnabled),
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                )
                Text(
                    text = stringResource(id = fileType.titleRes),
                    fontSize = 18.sp,
                    color = Color.Unspecified.orOnSurfaceDisabledIf(condition = !isEnabled)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showAutoMoveRow = !showAutoMoveRow }, enabled = isEnabled) {
                    Icon(
                        imageVector = if (showAutoMoveRow) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                    )
                }
                Switch(
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(8.dp),
                    checked = isEnabled,
                    onCheckedChange = onCheckedChange
                )
            }
            AnimatedVisibility(visible = isEnabled && showAutoMoveRow) {
                AutoMoveRow(path = "path/otherPath/folder/otherFolder", isEnabled = true)
            }
        }
    }
}

@Composable
private fun AutoMoveRow(path: String, isEnabled: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        HorizontalDivider()
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Auto Move",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {},
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.secondary,
                        checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                        checkedIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                Text(path, modifier = Modifier.weight(1f), fontSize = 14.sp)
                IconButton(onClick = { /*TODO*/ }, modifier = Modifier.size(38.dp)) {
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
}

@Preview
@Composable
private fun AutoMoveRowPrev() {
    AppTheme {
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AutoMoveRow(path = "/path/component/somefolder", isEnabled = true)
        }
    }
}

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