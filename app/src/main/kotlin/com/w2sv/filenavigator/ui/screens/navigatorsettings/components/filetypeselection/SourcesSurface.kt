package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.InterElementDividedColumn
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.AutoMoveRow
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.rememberAutoMoveDestinationPath
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.rememberSelectAutoMoveDestination
import com.w2sv.filenavigator.ui.utils.orOnSurfaceDisabledIf
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun SourcesSurface(
    fileType: FileType,
    sourceTypeConfigMap: ImmutableMap<SourceType, SourceConfig>,
    onSourceCheckedChange: (SourceType, Boolean) -> Unit,
    setSourceAutoMoveConfig: (SourceType, AutoMoveConfig) -> Unit,
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
                val sourceConfig = sourceTypeConfigMap.getValue(sourceType)
                val autoMoveConfig = sourceConfig.autoMoveConfig
                val selectAutoMoveDestination = rememberSelectAutoMoveDestination {
                    setSourceAutoMoveConfig(
                        sourceType,
                        autoMoveConfig.copy(enabled = true, destination = it)
                    )
                }
                SourceRow(
                    fileType = fileType,
                    sourceType = sourceType,
                    sourceConfig = sourceConfig,
                    onCheckedChange = { onSourceCheckedChange(sourceType, it) },
                    onAutoMoveEnabledCheckedChange = { checkedNew ->
                        if (checkedNew && autoMoveConfig.destination == null) {
                            selectAutoMoveDestination.launch(null)
                        } else {
                            setSourceAutoMoveConfig(
                                sourceType,
                                autoMoveConfig.copy(checkedNew)
                            )
                        }
                    },
                    modifier = Modifier.height(44.dp)
                )
                val autoMoveDestinationPath by rememberAutoMoveDestinationPath(destination = autoMoveConfig.destination)
                AnimatedVisibility(visible = sourceConfig.enabled && autoMoveConfig.enabled && autoMoveDestinationPath != null) {
                    autoMoveDestinationPath?.let { path ->
                        AutoMoveRow(
                            destinationPath = path,
                            changeDestination = { selectAutoMoveDestination.launch(autoMoveConfig.destination?.uri) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SourceRow(
    fileType: FileType,
    sourceType: SourceType,
    sourceConfig: SourceConfig,
    onCheckedChange: (Boolean) -> Unit,
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
        Spacer(modifier = Modifier.weight(1f))
        AnimatedVisibility(visible = sourceConfig.enabled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.height(32.dp)) {
                    MoveModeSegmentedButton(
                        selected = !sourceConfig.autoMoveConfig.enabled,
                        onClick = { onAutoMoveEnabledCheckedChange(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        text = "Notify"
                    )
                    MoveModeSegmentedButton(
                        selected = sourceConfig.autoMoveConfig.enabled,
                        onClick = { onAutoMoveEnabledCheckedChange(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        text = "Auto"
                    )
                }
            }
        }
        if (fileType.isMediaType) {
            Checkbox(
                checked = sourceConfig.enabled,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleChoiceSegmentedButtonRowScope.MoveModeSegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    text: String
) {
    SegmentedButton(
        selected = selected,
        onClick = onClick,
        shape = shape,
        icon = {},
        modifier = Modifier
            .fillMaxHeight()
            .width(72.dp)
    ) {
        Text(text = text, fontSize = 13.sp)
    }
}