package com.w2sv.filenavigator.ui.screens.navigatorsettings.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.common.utils.getDocumentUriPath
import com.w2sv.common.utils.takePersistableReadAndWriteUriPermission
import com.w2sv.composed.isPortraitModeActive
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.RightAligned
import com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection.FileTypeAccordion
import com.w2sv.filenavigator.ui.states.ReversibleNavigatorConfig
import kotlinx.collections.immutable.toImmutableMap
import slimber.log.i

private val verticalPadding = 16.dp

@Composable
fun SubDirectoryIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_subdirectory_arrow_right_24),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun rememberAutoMoveDestinationPath(
    destination: Uri?,
    context: Context = LocalContext.current
): State<String?> =
    remember(destination) {
        mutableStateOf(
            destination?.let { getDocumentUriPath(it, context) }
        )
    }

@Composable
fun AutoMoveRow(
    destinationPath: String,
    changeDestination: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    CompositionLocalProvider(value = LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .padding(start = 10.dp, bottom = 4.dp)
        ) {
            SubDirectoryIcon(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(20.dp)
            )
            Text(destinationPath, modifier = Modifier.weight(1f), fontSize = 14.sp)
            IconButton(
                onClick = { changeDestination() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_folder_edit_24),
                    contentDescription = stringResource(R.string.select_the_auto_move_destination),
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            trailing?.invoke(this)
        }
    }
}

@Composable
fun rememberSelectAutoMoveDestination(onDestinationSelected: (Uri) -> Unit): ManagedActivityResultLauncher<Uri?, Uri?> {
    val context: Context = LocalContext.current
    return rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { optionalTreeUri ->
        optionalTreeUri?.let { treeUri ->
            context.contentResolver.takePersistableReadAndWriteUriPermission(treeUri)
            onDestinationSelected(
                DocumentFile.fromTreeUri(context, treeUri)!!.uri
            )
        }
    }
}

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
                setSourceAutoMoveConfigs = { autoMoveConfig ->
                    reversibleConfig.updateAndCancelSnackbar {
                        it.copyWithAlteredSourceAutoMoveConfigs(
                            fileType = fileType,
                            autoMoveConfig = autoMoveConfig
                        )
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
                setSourceAutoMoveConfig = { sourceType, autoMoveConfig ->
                    reversibleConfig.updateAndCancelSnackbar {
                        it.copyWithAlteredSourceAutoMoveConfig(fileType, sourceType) {
                            autoMoveConfig
                        }
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
                    reversibleConfig.updateAndCancelSnackbar { it.copy(disableOnLowBattery = checked) }
                },
                startOnBoot = config.startOnBoot,
                setStartOnBoot = { checked ->
                    reversibleConfig.updateAndCancelSnackbar { it.copy(startOnBoot = checked) }
                },
                modifier = Modifier
                    .padding(bottom = if (isPortraitModeActive) 132.dp else 92.dp)
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
    disableOnLowBattery: Boolean,
    setDisableOnLowBattery: (Boolean) -> Unit,
    startOnBoot: Boolean,
    setStartOnBoot: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SwitchItemRow(
            iconRes = R.drawable.ic_battery_low_24,
            textRes = R.string.disable_on_low_battery,
            checked = disableOnLowBattery,
            onCheckedChange = setDisableOnLowBattery
        )
        SwitchItemRow(
            iconRes = R.drawable.ic_restart_24,
            textRes = R.string.start_on_system_boot,
            checked = startOnBoot,
            onCheckedChange = setStartOnBoot
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