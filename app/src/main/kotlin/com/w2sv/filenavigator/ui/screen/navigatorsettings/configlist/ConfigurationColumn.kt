package com.w2sv.filenavigator.ui.screen.navigatorsettings.configlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.designsystem.DefaultItemRowIcon
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.Spacing
import com.w2sv.filenavigator.ui.designsystem.SwitchItemRow
import com.w2sv.filenavigator.ui.state.ReversibleNavigatorConfig
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.update
import slimber.log.i

private val verticalPadding = 16.dp

@Composable
fun NavigatorConfigurationColumn(
    config: NavigatorConfig,
    reversibleConfig: ReversibleNavigatorConfig,
    showFileTypesBottomSheet: () -> Unit,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionHeader(text = stringResource(id = R.string.navigated_file_types))
                FilledTonalIconButton(onClick = showFileTypesBottomSheet) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.configure_the_used_file_types)
                    )
                }
            }
        }
        items(config.sortedEnabledFileTypes, key = { it.ordinal }) { fileType ->
            i { "Laying out ${fileType.logIdentifier}" }

            FileTypeAccordion(
                fileType = fileType,
                setSourceAutoMoveConfigs = remember(fileType) {
                    if (fileType.isMediaType) {
                        { autoMoveConfig ->
                            reversibleConfig.update {
                                it.updateAutoMoveConfigs(
                                    fileType = fileType,
                                    autoMoveConfig = autoMoveConfig
                                )
                            }
                        }
                    } else {
                        null
                    }
                },
                sourceTypeConfigMap = config.fileTypeConfig(fileType).sourceTypeConfigMap.toImmutableMap(),
                onSourceCheckedChange = remember(fileType) {
                    { source, checked ->
                        reversibleConfig.onFileSourceCheckedChange(
                            fileType = fileType,
                            sourceType = source,
                            checkedNew = checked
                        )
                    }
                },
                setSourceAutoMoveConfig = remember(fileType) {
                    { sourceType, autoMoveConfig ->
                        reversibleConfig.update {
                            it.updateAutoMoveConfig(fileType, sourceType) {
                                autoMoveConfig
                            }
                        }
                    }
                },
                showFileTypeConfigurationDialog = showFileTypeConfigurationDialog,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .animateItem()
            )
        }
        item {
            i { "Laying out MoreColumn" }

            SectionHeader(
                text = stringResource(id = R.string.more)
            )
            MoreColumnItems(
                showBatchMoveNotification = config.showBatchMoveNotification,
                setShowBatchMoveNotification = { checked ->
                    reversibleConfig.update { it.copy(showBatchMoveNotification = checked) }
                },
                disableOnLowBattery = config.disableOnLowBattery,
                setDisableOnLowBattery = { checked ->
                    reversibleConfig.update { it.copy(disableOnLowBattery = checked) }
                },
                startOnBoot = config.startOnBoot,
                setStartOnBoot = { checked ->
                    reversibleConfig.update { it.copy(startOnBoot = checked) }
                },
                modifier = Modifier
                    .padding(bottom = Padding.fabButtonBottomPadding)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
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
    showBatchMoveNotification: Boolean,
    setShowBatchMoveNotification: (Boolean) -> Unit,
    disableOnLowBattery: Boolean,
    setDisableOnLowBattery: (Boolean) -> Unit,
    startOnBoot: Boolean,
    setStartOnBoot: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.VerticalItemRow)
    ) {
        SwitchItemRow(
            icon = { DefaultItemRowIcon(res = R.drawable.ic_files_24) },
            labelRes = R.string.show_batch_move_notification,
            checked = showBatchMoveNotification,
            onCheckedChange = setShowBatchMoveNotification,
            explanation = stringResource(R.string.batch_move_explanation)
        )
        SwitchItemRow(
            icon = { DefaultItemRowIcon(res = R.drawable.ic_battery_low_24) },
            labelRes = R.string.disable_navigator_on_low_battery,
            checked = disableOnLowBattery,
            onCheckedChange = setDisableOnLowBattery
        )
        SwitchItemRow(
            icon = { DefaultItemRowIcon(res = R.drawable.ic_restart_24) },
            labelRes = R.string.start_navigator_on_system_boot,
            checked = startOnBoot,
            onCheckedChange = setStartOnBoot
        )
    }
}
