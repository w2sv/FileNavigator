package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.InterElementDividedColumn
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import com.w2sv.filenavigator.ui.designsystem.TweakedSwitch
import com.w2sv.filenavigator.ui.designsystem.drawer.AutoMoveIcon
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.AutoMoveRow
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.rememberAutoMoveDestinationPath
import com.w2sv.filenavigator.ui.utils.orOnSurfaceDisabledIf
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun FileTypeSourcesSurface(
    fileType: FileType,
    sourceTypeConfigMap: ImmutableMap<SourceType, SourceConfig>,
    onSourceCheckedChange: (SourceType, Boolean) -> Unit,
    onAutoMoveEnabledCheckedChange: (SourceType, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        InterElementDividedColumn(
            elements = fileType.sourceTypes,
            makeElement = { sourceType ->
                val sourceConfig = sourceTypeConfigMap[sourceType] ?: SourceConfig()  // TODO
                SourceRow(
                    fileType = fileType,
                    sourceType = sourceType,
                    sourceConfig = sourceConfig,
                    onCheckedChange = { onSourceCheckedChange(sourceType, it) },
                    autoMoveEnabled = sourceConfig.autoMoveConfig.enabled,
                    onAutoMoveEnabledCheckedChange = {
                        onAutoMoveEnabledCheckedChange(sourceType, it)
                    },
                    modifier = Modifier.height(44.dp)
                )
                val autoMoveDestinationPath by rememberAutoMoveDestinationPath(destination = sourceConfig.autoMoveConfig.destination)
                AnimatedVisibility(visible = sourceConfig.enabled && sourceConfig.autoMoveConfig.enabled && autoMoveDestinationPath != null) {
                    autoMoveDestinationPath?.let { path ->
                        AutoMoveRow(
                            destinationPath = path,
                            changeDestination = { /*TODO*/ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 22.dp, end = 10.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun SourceRow(
    fileType: FileType,
    sourceType: SourceType,
    sourceConfig: SourceConfig,
    onCheckedChange: (Boolean) -> Unit,
    autoMoveEnabled: Boolean,
    onAutoMoveEnabledCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = sourceType.iconRes),
            contentDescription = null,
            tint = fileType.color
                .orOnSurfaceDisabledIf(condition = !sourceConfig.enabled),
            modifier = Modifier.padding(start = 26.dp, end = 18.dp)
        )
        Text(
            text = stringResource(id = sourceType.labelRes),
            color = MaterialTheme.colorScheme.onSurface
                .orOnSurfaceDisabledIf(condition = !sourceConfig.enabled)
        )
        if (fileType.isMediaType) {
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = sourceConfig.enabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AutoMoveIcon(
                        modifier = Modifier.padding(end = 6.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
//                    TweakedSwitch(
//                        checked = autoMoveEnabled,
//                        onCheckedChange = onAutoMoveEnabledCheckedChange
//                    )
                }
            }
            Checkbox(
                checked = sourceConfig.enabled,
                onCheckedChange = onCheckedChange
            )
        }
    }
}