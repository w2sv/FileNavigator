package com.w2sv.filenavigator.ui.screen.navigatorsettings.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.w2sv.common.uri.DocumentUri
import com.w2sv.composed.material3.ColumnWithDividers
import com.w2sv.designsystem.component.TweakedSegmentedButton
import com.w2sv.designsystem.modelext.color
import com.w2sv.designsystem.theme.orOnSurfaceDisabledIf
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.FileTypeId
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.model.navigatorconfig.SourceConfig
import com.w2sv.filenavigator.ui.shared.debugging.PreviewOf
import com.w2sv.kotlinutils.copy
import com.w2sv.modules.resources.R
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

@Preview
@Composable
private fun Prev() {
    PreviewOf {
        FileTypeSourcesSettingsSurface(
            PresetFileType.Image.toFileType(),
            NavigatorConfig.default.fileTypeConfigMap.getValue(FileTypeId.Preset(PresetFileType.Image)).sourceTypeConfigMap.copy {
                this[SourceType.Camera] =
                    SourceConfig(
                        autoMoveConfig = AutoMoveConfig(
                            enabled = true,
                            destination = LocalDestination(DocumentUri("primary:Download/Zango/Nested".toUri()))
                        )
                    )
            }.toImmutableMap(),
            onSourceCheckedChange = { _, _ -> },
            setSourceAutoMoveConfig = { _, _ -> }
        )
    }
}

@Composable
fun FileTypeSourcesSettingsSurface(
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
        ColumnWithDividers(
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
                    enableAutoMove = { enable ->
                        if (enable && autoMoveConfig.destination == null) {
                            selectAutoMoveDestination.launch(null)
                        } else {
                            setSourceAutoMoveConfig(
                                sourceType,
                                autoMoveConfig.copy(enabled = enable)
                            )
                        }
                    },
                    modifier = Modifier.height(44.dp)
                )
                val autoMoveDestinationPath by rememberAutoMoveDestinationPath(destination = autoMoveConfig.destination)
                AnimatedVisibility(visible = sourceConfig.enabled && autoMoveConfig.enabled && autoMoveDestinationPath != null) {
                    autoMoveDestinationPath?.let { path ->
                        Text(
                            text = path,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 68.dp, end = 48.dp, bottom = 8.dp)
                                .clickable(
                                    onClick = { selectAutoMoveDestination.launch(autoMoveConfig.destination?.documentUri?.uri) },
                                    onClickLabel = stringResource(R.string.select_the_auto_move_destination)
                                ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
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
    enableAutoMove: (Boolean) -> Unit,
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
                .orOnSurfaceDisabledIf(condition = !sourceConfig.enabled),
            modifier = Modifier.weight(1f)
        )
        AnimatedVisibility(visible = sourceConfig.enabled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SingleChoiceSegmentedButtonRow {
                    SegmentedMoveModeButton(
                        selected = !sourceConfig.autoMoveConfig.enabled,
                        onClick = { enableAutoMove(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        text = stringResource(R.string.notify)
                    )
                    SegmentedMoveModeButton(
                        selected = sourceConfig.autoMoveConfig.enabled,
                        onClick = { enableAutoMove(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        text = stringResource(id = R.string.auto)
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

@Composable
private fun SingleChoiceSegmentedButtonRowScope.SegmentedMoveModeButton(
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    text: String
) {
    TweakedSegmentedButton(
        selected = selected,
        onClick = onClick,
        shape = shape,
        icon = {},
        modifier = Modifier
            .size(width = 70.dp, height = 30.dp)
    ) {
        Text(text = text, fontSize = 13.sp)
    }
}
