package com.w2sv.filenavigator.ui.screens.navigatorsettings.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.RightAligned
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection.FileTypeAccordion
import com.w2sv.filenavigator.ui.states.ReversibleNavigatorConfig
import kotlinx.coroutines.flow.update
import slimber.log.i

private val verticalPadding = 16.dp

@Composable
fun NavigatorConfigurationColumn(
    reversibleConfig: ReversibleNavigatorConfig,
    showAddFileTypesBottomSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config by reversibleConfig.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionHeader(
                    text = stringResource(id = R.string.file_types),
                )
                AnimatedVisibility(visible = config.disabledFileTypes.isNotEmpty()) {
                    FilledTonalIconButton(onClick = showAddFileTypesBottomSheet) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_a_file_type)
                        )
                    }
                }
            }
        }
        items(config.enabledFileTypes, key = { it }) { fileType ->
            i { "Laying out ${fileType.logIdentifier}" }

            FileTypeAccordion(
                fileType = fileType,
                excludeFileType = remember(fileType) {
                    {
                        reversibleConfig.onFileTypeCheckedChange(
                            fileType = fileType,
                            checkedNew = false
                        )
                    }
                },
                autoMoveConfig = config.fileTypeConfig(fileType).autoMoveConfig,
                setAutoMoveConfig = { autoMoveConfig ->
                    reversibleConfig.update {
                        it.copyWithAlteredFileAutoMoveConfig(
                            fileType = fileType,
                            autoMoveConfig = autoMoveConfig
                        )
                    }
                },
                mediaFileSourceEnabled = remember(fileType) {
                    { sourceType ->
                        config.fileTypeConfigMap.getValue(fileType).sourceTypeToConfig[sourceType]?.enabled
                            ?: true
                    }
                },
                onMediaFileSourceCheckedChange = remember(fileType) {
                    { source, checked ->
                        reversibleConfig.onMediaFileSourceCheckedChange(
                            fileType = fileType,
                            sourceType = source,
                            checkedNew = checked
                        )
                    }
                },
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .animateItem()
            )
        }
        item {
            SectionHeader(
                text = stringResource(id = R.string.more),
            )
            MoreColumnItems(
                disableOnLowBattery = config.disableOnLowBattery,
                setDisableOnLowBattery = { checked ->
                    reversibleConfig.update { it.copy(disableOnLowBattery = checked) }
                },
                modifier = Modifier.padding(bottom = if (isPortraitModeActive) 132.dp else 92.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = defaultSectionHeaderModifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
    )
}

private val defaultSectionHeaderModifier = Modifier.padding(vertical = verticalPadding)

@Composable
private fun MoreColumnItems(
    disableOnLowBattery: Boolean,
    setDisableOnLowBattery: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SwitchItemRow(
            iconRes = R.drawable.ic_battery_low_24,
            textRes = R.string.disable_on_low_battery,
            checked = disableOnLowBattery,
            onCheckedChange = setDisableOnLowBattery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun SwitchItemRow(
    @DrawableRes iconRes: Int,
    @StringRes textRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = stringResource(id = textRes))
        RightAligned {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}