package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.model.color
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
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = fileType.iconRes),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(34.dp),
                tint = fileType.color.orOnSurfaceDisabledIf(condition = !isEnabled)
            )
            Text(
                text = stringResource(id = fileType.titleRes),
                fontSize = 18.sp,
                color = Color.Unspecified.orOnSurfaceDisabledIf(condition = !isEnabled)
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { }
                    .padding(horizontal = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_new_folder_24),
                    contentDescription = null,
                )
                Text(text = "Auto", fontSize = 12.sp)
            }
            Switch(
                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(8.dp),
                checked = isEnabled,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
